package com.cravedrop.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Persists a customer's saved menu items. The table is created on first use
 * so existing CraveDrop databases are upgraded without a manual migration. */
public class WishlistDAO {
    private static volatile boolean ready;

    private void ensureTable() throws SQLException {
        if (ready) return;
        synchronized (WishlistDAO.class) {
            if (ready) return;
            try (var c = Db.get(); var s = c.createStatement()) {
                s.executeUpdate("CREATE TABLE IF NOT EXISTS wishlist_items ("
                        + "user_id INT NOT NULL, item_id INT NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                        + "PRIMARY KEY(user_id,item_id),"
                        + "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,"
                        + "FOREIGN KEY(item_id) REFERENCES menu_items(id) ON DELETE CASCADE)");
            }
            ready = true;
        }
    }

    public boolean toggle(int userId, int itemId) throws SQLException {
        ensureTable();
        try (var c = Db.get(); var check = c.prepareStatement(
                "SELECT 1 FROM wishlist_items WHERE user_id=? AND item_id=?")) {
            check.setInt(1, userId); check.setInt(2, itemId);
            try (var rows = check.executeQuery()) {
                if (rows.next()) {
                    try (var delete = c.prepareStatement("DELETE FROM wishlist_items WHERE user_id=? AND item_id=?")) {
                        delete.setInt(1, userId); delete.setInt(2, itemId); delete.executeUpdate();
                    }
                    return false;
                }
            }
            try (var insert = c.prepareStatement("INSERT INTO wishlist_items(user_id,item_id) VALUES(?,?)")) {
                insert.setInt(1, userId); insert.setInt(2, itemId); insert.executeUpdate();
            }
            return true;
        }
    }

    public List<Integer> itemIds(int userId) throws SQLException {
        ensureTable();
        var ids = new ArrayList<Integer>();
        try (var c = Db.get(); var p = c.prepareStatement(
                "SELECT item_id FROM wishlist_items WHERE user_id=? ORDER BY created_at DESC")) {
            p.setInt(1, userId);
            try (var rows = p.executeQuery()) { while (rows.next()) ids.add(rows.getInt(1)); }
        }
        return ids;
    }
}
