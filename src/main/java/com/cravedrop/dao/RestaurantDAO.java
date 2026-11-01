package com.cravedrop.dao;

import com.cravedrop.model.Restaurant;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Loads the 30 restaurant cards from the adapted photo catalog resource. */
public class RestaurantDAO {
    private static final List<Restaurant> RESTAURANTS = loadCatalog();

    private static List<Restaurant> loadCatalog() {
        var stream = RestaurantDAO.class.getResourceAsStream("/catalog/restaurants.tsv");
        if (stream == null) throw new ExceptionInInitializerError("Missing /catalog/restaurants.tsv");

        var restaurants = new ArrayList<Restaurant>();
        try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue;
                String[] fields = line.split("\\t", -1);
                if (fields.length != 8) throw new IllegalStateException("Invalid restaurant catalog row " + lineNumber);
                restaurants.add(new Restaurant(fields[0], fields[1], fields[2], fields[3],
                        Double.parseDouble(fields[4]), Integer.parseInt(fields[5]),
                        Boolean.parseBoolean(fields[6]), fields[7]));
            }
        } catch (IOException | NumberFormatException e) {
            throw new ExceptionInInitializerError(e);
        }
        return List.copyOf(restaurants);
    }

    public List<Restaurant> list() { return RESTAURANTS; }

    public Restaurant find(String name) {
        return RESTAURANTS.stream().filter(r -> r.name().equals(name)).findFirst().orElse(null);
    }
}
