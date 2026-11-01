package com.cravedrop.model;

public record Restaurant(String name, String cuisine, String category, String image,
                         double rating, int minutes, boolean veg, String area) {
    // JavaBean accessors keep the record fields visible to Eclipse's JSP validator.
    public String getName() { return name; }
    public String getCuisine() { return cuisine; }
    public String getCategory() { return category; }
    public String getImage() { return image; }
    public double getRating() { return rating; }
    public int getMinutes() { return minutes; }
    public boolean isVeg() { return veg; }
    public String getArea() { return area; }
}
