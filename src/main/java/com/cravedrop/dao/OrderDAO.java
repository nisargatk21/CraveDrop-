package com.cravedrop.dao;

import com.cravedrop.model.Models.*;
import java.sql.*;
import java.util.*;

public class OrderDAO {
    /** Saves the order header and all lines in one transaction. */
    public int create(int userId, double total, String payment, String address, String phone,
                      List<CartLine> lines) throws SQLException {
        try (var connection = Db.get()) {
            connection.setAutoCommit(false);
            try (var insertOrder = connection.prepareStatement(
                    "INSERT INTO orders(user_id,total,payment_method,address,phone) VALUES(?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                insertOrder.setInt(1, userId);
                insertOrder.setDouble(2, total);
                insertOrder.setString(3, payment);
                insertOrder.setString(4, address);
                insertOrder.setString(5, phone);
                insertOrder.executeUpdate();
                int orderId;
                try (var keys = insertOrder.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("The order id was not returned.");
                    orderId = keys.getInt(1);
                }
                try (var insertLine = connection.prepareStatement(
                        "INSERT INTO order_items(order_id,item_name,qty,price) VALUES(?,?,?,?)")) {
                    for (var line : lines) {
                        insertLine.setInt(1, orderId);
                        insertLine.setString(2, line.item().name());
                        insertLine.setInt(3, line.qty());
                        insertLine.setDouble(4, line.item().price());
                        insertLine.addBatch();
                    }
                    insertLine.executeBatch();
                }
                connection.commit();
                return orderId;
            } catch (SQLException error) {
                connection.rollback();
                throw error;
            }
        }
    }

    public List<Order> listByUser(int userId) throws SQLException {
        var orders = new ArrayList<Order>();
        try (var connection = Db.get();
             var selectOrders = connection.prepareStatement(
                     "SELECT o.*, (SELECT COUNT(*) FROM orders earlier " +
                     "WHERE earlier.user_id=o.user_id AND earlier.id<=o.id) AS customer_order_number " +
                     "FROM orders o WHERE o.user_id=? ORDER BY o.id ASC");
             var selectLines = connection.prepareStatement("SELECT item_name,qty,price FROM order_items WHERE order_id=?")) {
            selectOrders.setInt(1, userId);
            try (var rows = selectOrders.executeQuery()) {
                while (rows.next()) orders.add(readOrder(rows, selectLines));
            }
        }
        return orders;
    }

    /** A confirmation can only be opened by the account that placed that order. */
    public Order findByUser(int userId, int orderId) throws SQLException {
        try (var connection = Db.get();
             var selectOrder = connection.prepareStatement(
                     "SELECT o.*, (SELECT COUNT(*) FROM orders earlier " +
                     "WHERE earlier.user_id=o.user_id AND earlier.id<=o.id) AS customer_order_number " +
                     "FROM orders o WHERE o.user_id=? AND o.id=?");
             var selectLines = connection.prepareStatement("SELECT item_name,qty,price FROM order_items WHERE order_id=?")) {
            selectOrder.setInt(1, userId);
            selectOrder.setInt(2, orderId);
            try (var rows = selectOrder.executeQuery()) {
                return rows.next() ? readOrder(rows, selectLines) : null;
            }
        }
    }

    private Order readOrder(ResultSet row, PreparedStatement selectLines) throws SQLException {
        int orderId = row.getInt("id");
        var lines = new ArrayList<Line>();
        selectLines.setInt(1, orderId);
        try (var results = selectLines.executeQuery()) {
            while (results.next()) lines.add(new Line(results.getString(1), results.getInt(2), results.getDouble(3)));
        }
        return new Order(orderId, row.getInt("customer_order_number"), row.getDouble("total"), row.getString("status"),
                row.getString("payment_method"), row.getString("address"), row.getTimestamp("created_at"), lines);
    }
}
