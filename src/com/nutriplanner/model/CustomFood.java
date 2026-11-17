package com.nutriplanner.model;

/**
 * A food that a user added to the database.
 *
 * It inherits everything from {@link Food} and adds the name of the user who
 * created it. It overrides two methods, so the rest of the program can treat
 * built-in and custom foods the same way (inheritance and polymorphism).
 */
public class CustomFood extends Food {

    private final String addedBy;

    public CustomFood(int id, String name, FoodCategory category, String servingSize,
                      double calories, double protein, double carbohydrates, double fat,
                      DietType dietType, String addedBy) {
        super(id, name, category, servingSize, calories, protein, carbohydrates, fat, dietType);
        this.addedBy = addedBy;
    }

    public String getAddedBy() {
        return addedBy;
    }

    @Override
    public boolean isBuiltIn() {
        return false;
    }

    @Override
    public String getSourceLabel() {
        return "Added by user '" + addedBy + "'";
    }
}
