package com.cravedrop.dao;

import com.cravedrop.model.Models.Item;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

/** Restaurant-specific menu reads and idempotent demo-catalog migration. */
public class MenuDAO {
    private record Seed(String restaurant, String category, String name, String description,
                       double price, String photo, boolean vegetarian) {}

    private static volatile boolean catalogReady;
    private static final List<Seed> SEEDS = loadSeeds();
    private static final Map<String, Set<String>> CATALOG_KEYS = buildCatalogKeys();
    private static final Set<String> ACTIVE_KEYS = SEEDS.stream().map(item -> seedKey(item.category(), item.name())).collect(java.util.stream.Collectors.toUnmodifiableSet());

    private static List<Seed> loadSeeds() {
        InputStream stream = MenuDAO.class.getResourceAsStream("/catalog/menu-items.tsv");
        if (stream == null) throw new ExceptionInInitializerError("Missing /catalog/menu-items.tsv");
        var out = new ArrayList<Seed>();
        try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue;
                String[] fields = line.split("\\t", -1);
                if (fields.length != 7) throw new IllegalStateException("Invalid menu catalog row " + lineNumber);
                out.add(new Seed(fields[0], fields[1], fields[2], fields[3],
                        Double.parseDouble(fields[4]), fields[5], Boolean.parseBoolean(fields[6])));
            }
        } catch (IOException | NumberFormatException e) {
            throw new ExceptionInInitializerError(e);
        }
        return List.copyOf(out);
    }

    private static Map<String, Set<String>> buildCatalogKeys() {
        var keys = new HashMap<String, Set<String>>();
        for (Seed item : SEEDS) {
            keys.computeIfAbsent(item.restaurant(), ignored -> new HashSet<>())
                    .add(seedKey(item.category(), item.name()));
        }
        keys.replaceAll((restaurant, names) -> Set.copyOf(names));
        return Map.copyOf(keys);
    }

    private static String seedKey(String category, String name) {
        return category + '\t' + name;
    }
    private static List<Item> uniqueItems(List<Item> rows) {
        var unique = new LinkedHashMap<String, Item>();
        for (Item item : rows) {
            String key = item.restaurant() + '\t' + seedKey(item.category(), item.name());
            unique.putIfAbsent(key, item);
        }
        return List.copyOf(unique.values());
    }
    /** Search words for the restaurant cards, built from the bundled catalogue without a second DB query. */
    public Map<String, String> searchIndex() {
        var terms = new LinkedHashMap<String, String>();
        for (Seed item : SEEDS) terms.merge(item.restaurant(), item.name() + " " + item.category(), (a, b) -> a + " " + b);
        return Map.copyOf(terms);
    }
    static Item map(ResultSet r) throws SQLException {
        return new Item(r.getInt("id"), r.getString("restaurant_name"), r.getString("category"), r.getString("name"),
                r.getString("description"), r.getDouble("price"), r.getString("image_url"),
                r.getBoolean("is_veg"));
    }

    private void ensureColumn(Connection c, String column, String ddl) throws SQLException {
        try (var columns = c.getMetaData().getColumns(c.getCatalog(), null, "menu_items", column)) {
            if (columns.next()) return;
        }
        try (var s = c.createStatement()) { s.executeUpdate(ddl); }
    }

    /** Upgrades old databases and attaches every catalog item to one restaurant. */
    private void ensureCatalog() throws SQLException {
        if (catalogReady) return;
        synchronized (MenuDAO.class) {
            if (catalogReady) return;
            try (var c = Db.get()) {
                ensureColumn(c, "is_veg", "ALTER TABLE menu_items ADD COLUMN is_veg BOOLEAN NOT NULL DEFAULT TRUE");
                ensureColumn(c, "restaurant_name", "ALTER TABLE menu_items ADD COLUMN restaurant_name VARCHAR(100) NULL");
                try (var s = c.createStatement()) {
                    s.executeUpdate("UPDATE menu_items SET is_veg=FALSE WHERE name IN ('Pepperoni Feast','Classic Cheeseburger','Crispy Chicken Burger')");
                    s.executeUpdate("UPDATE menu_items SET restaurant_name=CASE category WHEN 'Pizza' THEN 'Pizza Hut' WHEN 'Burgers' THEN 'Burger King' WHEN 'Salads' THEN 'The Bowl Company' WHEN 'Desserts' THEN 'Truffles' ELSE restaurant_name END WHERE restaurant_name IS NULL");
                }
                boolean originalAutoCommit = c.getAutoCommit();
                c.setAutoCommit(false);
                try (var find = c.prepareStatement("SELECT id FROM menu_items WHERE restaurant_name=? AND category=? AND name=? LIMIT 1");
                     var update = c.prepareStatement("UPDATE menu_items SET restaurant_name=?,description=?,price=?,image_url=?,is_veg=? WHERE id=?");
                     var insert = c.prepareStatement("INSERT INTO menu_items(restaurant_name,category,name,description,price,image_url,is_veg) VALUES(?,?,?,?,?,?,?)")) {
                    for (Seed item : SEEDS) {
                        find.setString(1, item.restaurant());
                        find.setString(2, item.category());
                        find.setString(3, item.name());
                        Integer itemId = null;
                        try (var rows = find.executeQuery()) { if (rows.next()) itemId = rows.getInt(1); }
                        String image = item.photo();
                        if (itemId != null) {
                            update.setString(1, item.restaurant()); update.setString(2, item.description());
                            update.setDouble(3, item.price()); update.setString(4, image);
                            update.setBoolean(5, item.vegetarian()); update.setInt(6, itemId); update.executeUpdate();
                        } else {
                            insert.setString(1, item.restaurant()); insert.setString(2, item.category());
                            insert.setString(3, item.name()); insert.setString(4, item.description());
                            insert.setDouble(5, item.price()); insert.setString(6, image);
                            insert.setBoolean(7, item.vegetarian()); insert.executeUpdate();
                        }
                    }
                    c.commit();
                } catch (SQLException e) {
                    c.rollback(); throw e;
                } finally { c.setAutoCommit(originalAutoCommit); }
            }
            catalogReady = true;
        }
    }

    public List<Item> listForRestaurant(String restaurant, String q, String veg) throws SQLException {
        ensureCatalog();
        var sql = new StringBuilder("SELECT * FROM menu_items WHERE restaurant_name=?");
        var args = new ArrayList<String>(); args.add(restaurant);
        if ("true".equals(veg) || "false".equals(veg)) { sql.append(" AND is_veg=?"); args.add("true".equals(veg) ? "1" : "0"); }
        if (q != null && !q.isBlank()) {
            sql.append(" AND (name LIKE ? OR description LIKE ? OR category LIKE ?)");
            for (int i = 0; i < 3; i++) args.add("%" + q.trim() + "%");
        }
        sql.append(" ORDER BY category,name");
        var out = new ArrayList<Item>();
        try (var c = Db.get(); var p = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < args.size(); i++) p.setString(i + 1, args.get(i));
            try (var r = p.executeQuery()) { while (r.next()) out.add(map(r)); }
        }
        Set<String> activeItems = CATALOG_KEYS.get(restaurant);
        if (activeItems != null) out.removeIf(item -> !activeItems.contains(seedKey(item.category(), item.name())));
        out.removeIf(item -> item.restaurant() == null || !ACTIVE_KEYS.contains(seedKey(item.category(), item.name())));
        return uniqueItems(out);
    }

    public List<Item> list(String category, String q, String veg) throws SQLException {
        ensureCatalog();
        var sql = new StringBuilder("SELECT * FROM menu_items WHERE 1=1");
        var args = new ArrayList<String>();
        if (category != null && !category.isBlank()) { sql.append(" AND category=?"); args.add(category); }
        if ("true".equals(veg) || "false".equals(veg)) { sql.append(" AND is_veg=?"); args.add("true".equals(veg) ? "1" : "0"); }
        if (q != null && !q.isBlank()) { sql.append(" AND (name LIKE ? OR description LIKE ? OR category LIKE ?)"); for (int i = 0; i < 3; i++) args.add("%" + q.trim() + "%"); }
        sql.append(" ORDER BY category,name");
        var out = new ArrayList<Item>();
        try (var c = Db.get(); var p = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < args.size(); i++) p.setString(i + 1, args.get(i));
            try (var r = p.executeQuery()) { while (r.next()) out.add(map(r)); }
        }
        out.removeIf(item -> item.restaurant() == null || !ACTIVE_KEYS.contains(seedKey(item.category(), item.name())));
        return uniqueItems(out);
    }

    public List<String> categories() throws SQLException {
        ensureCatalog(); var out = new ArrayList<String>();
        try (var c = Db.get(); var s = c.createStatement(); var r = s.executeQuery("SELECT DISTINCT category FROM menu_items ORDER BY category")) { while (r.next()) out.add(r.getString(1)); }
        return out;
    }

    /** One menu pick per restaurant, so the home page highlights don't repeat the same restaurant. */
    public List<Item> featured(int limit) throws SQLException {
        ensureCatalog();
        var result = new ArrayList<Item>();
        var seen = new HashSet<String>();
        String sql = "SELECT * FROM menu_items ORDER BY restaurant_name,category,name";
        try (var c = Db.get(); var s = c.createStatement(); var r = s.executeQuery(sql)) {
            while (result.size() < Math.max(1, limit) && r.next()) {
                Item item = map(r);
                // Older project databases may still contain unattached catalog rows.
                // Map.of-backed CATALOG_KEYS rejects null lookup keys.
                if (item.restaurant() == null || item.restaurant().isBlank()) continue;
                Set<String> restaurantItems = CATALOG_KEYS.get(item.restaurant());
                if (restaurantItems != null && restaurantItems.contains(seedKey(item.category(), item.name()))
                        && ACTIVE_KEYS.contains(seedKey(item.category(), item.name())) && seen.add(item.restaurant())) result.add(item);
            }
        }
        return List.copyOf(result);
    }

    public Map<Integer, Item> findByIds(Collection<Integer> ids) throws SQLException {
        ensureCatalog(); var items = new HashMap<Integer, Item>(); if (ids.isEmpty()) return items;
        try (var c = Db.get(); var p = c.prepareStatement("SELECT * FROM menu_items WHERE id IN (" + String.join(",", Collections.nCopies(ids.size(), "?")) + ")")) {
            int i = 1; for (int id : ids) p.setInt(i++, id);
            try (var r = p.executeQuery()) { while (r.next()) { var item = map(r); items.put(item.id(), item); } }
        }
        items.values().removeIf(item -> item.restaurant() == null || !ACTIVE_KEYS.contains(seedKey(item.category(), item.name())));
        return items;
    }
}
