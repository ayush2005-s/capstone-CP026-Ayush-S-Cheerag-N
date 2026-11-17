package com.nutriplanner.model;

/**
 * The four meals of the day, in the order they are eaten.
 */
public enum MealType implements Labeled {

    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    EVENING_SNACKS("Evening Snacks"),
    DINNER("Dinner");

    private final String label;

    MealType(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
