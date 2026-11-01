package com.cravedrop.dao;

import com.cravedrop.model.Models.AdminOrder;
import com.cravedrop.model.Models.AdminStats;
import com.cravedrop.model.Models.AdminUser;
import com.cravedrop.model.Models.AdminDeliveryPartner;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Database-backed data and operations used by the protected administrator dashboard. */
public class AdminDashboardDAO {
    public void ensureDeliverySchema() throws SQLException {
        try (var c = Db.get(); var s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS delivery_partners (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(80) NOT NULL," +
                    "phone CHAR(10) NOT NULL UNIQUE," +
                    "area VARCHAR(100) NOT NULL," +
                    "status ENUM('Available','On delivery','Offline') NOT NULL DEFAULT 'Available'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
        try (var c = Db.get(); var columns = c.getMetaData().getColumns(c.getCatalog(), null, "orders", "delivery_partner_id")) {
            if (!columns.next()) try (var s = c.createStatement()) {
                s.executeUpdate("ALTER TABLE orders ADD COLUMN delivery_partner_id INT NULL");
            }
        }
    }

    public AdminStats stats() throws SQLException {
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM users WHERE role='customer') customers, " +
                "(SELECT COUNT(*) FROM users WHERE role='owner') owners, " +
                "(SELECT COUNT(*) FROM menu_items) menu_items, " +
                "(SELECT COUNT(*) FROM orders) orders_count, " +
                "(SELECT COUNT(*) FROM orders WHERE status<>'Delivered') open_orders, " +
                "(SELECT COALESCE(SUM(total),0) FROM orders) revenue";
        try (var c = Db.get(); var s = c.createStatement(); var r = s.executeQuery(sql)) {
            if (!r.next()) throw new SQLException("Dashboard summary was not returned.");
            return new AdminStats(r.getInt("customers"), r.getInt("owners"), r.getInt("menu_items"),
                    r.getInt("orders_count"), r.getInt("open_orders"), r.getDouble("revenue"));
        }
    }

    public List<AdminOrder> recentOrders() throws SQLException {
        var result = new ArrayList<AdminOrder>();
        String sql = "SELECT o.id,u.name,u.email,o.total,o.status,o.created_at,o.delivery_partner_id,dp.name AS partner_name " +
                "FROM orders o JOIN users u ON u.id=o.user_id LEFT JOIN delivery_partners dp ON dp.id=o.delivery_partner_id " +
                "ORDER BY o.created_at DESC,o.id DESC LIMIT 25";
        try (var c = Db.get(); var s = c.createStatement(); var r = s.executeQuery(sql)) {
            while (r.next()) result.add(new AdminOrder(r.getInt("id"), r.getString("name"),
                    r.getString("email"), r.getDouble("total"), r.getString("status"), r.getTimestamp("created_at"),
                    r.getInt("delivery_partner_id"), r.getString("partner_name")));
        }
        return List.copyOf(result);
    }

    public List<AdminUser> recentUsers() throws SQLException {
        var result = new ArrayList<AdminUser>();
        String sql = "SELECT id,name,email,phone,role,created_at FROM users ORDER BY created_at DESC,id DESC LIMIT 12";
        try (var c = Db.get(); var s = c.createStatement(); var r = s.executeQuery(sql)) {
            while (r.next()) result.add(new AdminUser(r.getInt("id"), r.getString("name"), r.getString("email"),
                    r.getString("phone"), r.getString("role"), r.getTimestamp("created_at")));
        }
        return List.copyOf(result);
    }

    public boolean updateOrderStatus(int orderId, String status) throws SQLException {
        if (orderId < 1 || !List.of("Placed", "Preparing", "Out for delivery", "Delivered").contains(status)) {
            throw new IllegalArgumentException("Choose a valid order status.");
        }
        try (var c = Db.get()) {
            c.setAutoCommit(false);
            try (var find = c.prepareStatement("SELECT delivery_partner_id FROM orders WHERE id=? FOR UPDATE")) {
                find.setInt(1, orderId);
                Integer partnerId;
                try (var row = find.executeQuery()) {
                    if (!row.next()) { c.rollback(); return false; }
                    int id = row.getInt(1); partnerId = row.wasNull() ? null : id;
                }
                try (var update = c.prepareStatement("UPDATE orders SET status=? WHERE id=?")) {
                    update.setString(1, status); update.setInt(2, orderId); update.executeUpdate();
                }
                if (partnerId != null) try (var partner = c.prepareStatement("UPDATE delivery_partners SET status=? WHERE id=?")) {
                    partner.setString(1, "Delivered".equals(status) ? "Available" : "On delivery"); partner.setInt(2, partnerId); partner.executeUpdate();
                }
                c.commit();
                return true;
            } catch (SQLException | RuntimeException error) { c.rollback(); throw error; }
            finally { c.setAutoCommit(true); }
        }
    }

    public List<AdminDeliveryPartner> deliveryPartners() throws SQLException {
        var result = new ArrayList<AdminDeliveryPartner>();
        String sql = "SELECT id,name,phone,area,status FROM delivery_partners ORDER BY name";
        try (var c = Db.get(); var s = c.createStatement(); var r = s.executeQuery(sql)) {
            while (r.next()) result.add(new AdminDeliveryPartner(r.getInt("id"), r.getString("name"),
                    r.getString("phone"), r.getString("area"), r.getString("status")));
        }
        return List.copyOf(result);
    }

    public void addDeliveryPartner(String name, String phone, String area) throws SQLException {
        String cleanName = name == null ? "" : name.trim();
        String cleanPhone = phone == null ? "" : phone.trim();
        String cleanArea = area == null ? "" : area.trim();
        if (cleanName.length() < 2 || cleanName.length() > 80 || !cleanPhone.matches("[0-9]{10}")
                || cleanArea.length() < 2 || cleanArea.length() > 100) {
            throw new IllegalArgumentException("Enter a partner name, 10-digit phone number, and delivery area.");
        }
        try (var c = Db.get(); var p = c.prepareStatement("INSERT INTO delivery_partners(name,phone,area) VALUES(?,?,?)")) {
            p.setString(1, cleanName); p.setString(2, cleanPhone); p.setString(3, cleanArea); p.executeUpdate();
        }
    }

    public void assignPartner(int orderId, int partnerId) throws SQLException {
        if (orderId < 1 || partnerId < 1) throw new IllegalArgumentException("Choose an order and a delivery partner.");
        try (var c = Db.get()) {
            c.setAutoCommit(false);
            try {
                Integer previousPartner;
                String orderStatus;
                try (var findOrder = c.prepareStatement("SELECT status,delivery_partner_id FROM orders WHERE id=? FOR UPDATE")) {
                    findOrder.setInt(1, orderId);
                    try (var row = findOrder.executeQuery()) {
                        if (!row.next()) throw new IllegalArgumentException("Order not found.");
                        orderStatus = row.getString("status");
                        int previous = row.getInt("delivery_partner_id"); previousPartner = row.wasNull() ? null : previous;
                    }
                }
                if ("Delivered".equals(orderStatus)) throw new IllegalArgumentException("A delivered order cannot be reassigned.");
                String partnerStatus;
                try (var findPartner = c.prepareStatement("SELECT status FROM delivery_partners WHERE id=? FOR UPDATE")) {
                    findPartner.setInt(1, partnerId);
                    try (var row = findPartner.executeQuery()) {
                        if (!row.next()) throw new IllegalArgumentException("Delivery partner not found.");
                        partnerStatus = row.getString(1);
                    }
                }
                if (!"Available".equals(partnerStatus) && !Objects.equals(previousPartner, partnerId))
                    throw new IllegalArgumentException("That delivery partner is already assigned to another order.");
                try (var updateOrder = c.prepareStatement("UPDATE orders SET delivery_partner_id=? WHERE id=?")) {
                    updateOrder.setInt(1, partnerId); updateOrder.setInt(2, orderId); updateOrder.executeUpdate();
                }
                if (previousPartner != null && previousPartner != partnerId) try (var release = c.prepareStatement("UPDATE delivery_partners SET status='Available' WHERE id=?")) {
                    release.setInt(1, previousPartner); release.executeUpdate();
                }
                try (var occupy = c.prepareStatement("UPDATE delivery_partners SET status='On delivery' WHERE id=?")) {
                    occupy.setInt(1, partnerId); occupy.executeUpdate();
                }
                c.commit();
            } catch (SQLException | RuntimeException error) { c.rollback(); throw error; }
            finally { c.setAutoCommit(true); }
        }
    }
}
