package com.nutriplanner.model;

/**
 * A dietary preference of a user, and the diet type of a food.
 *
 * The same enum is used for both, so checking whether a food suits a
 * user is a single method call: {@code user.getDietType().allows(food.getDietType())}.
 */
public enum DietType implements Labeled {

    VEGETARIAN("Vegetarian", "Veg"),
    NON_VEGETARIAN("Non-Vegetarian", "Non-veg"),
    VEGAN("Vegan", "Vegan");

    private final String label;
    private final String shortLabel;

    DietType(String label, String shortLabel) {
        this.label = label;
        this.shortLabel = shortLabel;
    }

    @Override
    public String getLabel() {
        return label;
    }

    /** Short form used in narrow table columns. */
    public String getShortLabel() {
        return shortLabel;
    }

    /**
     * Returns true when a person with this preference can eat a food of the given type.
     * Vegans eat only vegan food, vegetarians eat vegan and vegetarian food,
     * and non-vegetarians can eat everything.
     */
    public boolean allows(DietType foodType) {
        switch (this) {
            case VEGAN:
                return foodType == VEGAN;
            case VEGETARIAN:
                return foodType == VEGAN || foodType == VEGETARIAN;
            default:
                return true;
        }
    }
}
