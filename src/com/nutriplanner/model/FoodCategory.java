package com.nutriplanner.model;

/**
 * Groups used to organise the food database.
 */
public enum FoodCategory implements Labeled {

    BREAKFAST("Breakfast dishes"),
    RICE_AND_BREADS("Rice and breads"),
    DALS_AND_CURRIES("Dals and curries"),
    VEGETABLES("Vegetables and salads"),
    FRUITS("Fruits"),
    DAIRY("Milk and dairy"),
    PROTEIN_FOODS("Eggs, meat, fish and soy"),
    SNACKS("Snacks and sides"),
    BEVERAGES("Beverages"),
    NUTS_AND_FATS("Nuts, seeds and fats"),
    OTHER("Other foods");

    private final String label;

    FoodCategory(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
