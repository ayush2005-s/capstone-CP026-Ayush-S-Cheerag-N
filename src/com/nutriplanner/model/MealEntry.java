package com.nutriplanner.model;

import com.nutriplanner.exception.ValidationException;

/**
 * One food added to a meal, with the number of servings eaten.
 *
 * The food's name and per-serving values are copied into the entry when it is
 * created. This keeps old meal records correct even if the food database
 * changes later.
 */
public final class MealEntry implements NutritionalValue {

    public static final double MIN_SERVINGS = 0.25;
    public static final double MAX_SERVINGS = 20;

    private final int foodId;
    private final String foodName;
    private final String servingSize;
    private final double caloriesPerServing;
    private final double proteinPerServing;
    private final double carbohydratesPerServing;
    private final double fatPerServing;
    private double servings;

    public MealEntry(int foodId, String foodName, String servingSize, double caloriesPerServing,
                     double proteinPerServing, double carbohydratesPerServing, double fatPerServing,
                     double servings) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.servingSize = servingSize;
        this.caloriesPerServing = caloriesPerServing;
        this.proteinPerServing = proteinPerServing;
        this.carbohydratesPerServing = carbohydratesPerServing;
        this.fatPerServing = fatPerServing;
        setServings(servings);
    }

    /** Creates an entry for the given food and number of servings. */
    public static MealEntry fromFood(Food food, double servings) {
        return new MealEntry(food.getId(), food.getName(), food.getServingSize(), food.getCalories(),
                food.getProtein(), food.getCarbohydrates(), food.getFat(), servings);
    }

    /** Checks that a serving quantity is sensible, for example 0.5, 1 or 2. */
    public static void validateServings(double servings) {
        if (Double.isNaN(servings) || servings <= 0) {
            throw new ValidationException("Servings must be greater than zero.");
        }
        if (servings < MIN_SERVINGS || servings > MAX_SERVINGS) {
            throw new ValidationException("Servings must be between 0.25 and 20.");
        }
    }

    public void setServings(double servings) {
        validateServings(servings);
        // Keep at most two decimal places, for example 1.5 or 0.25.
        this.servings = Math.round(servings * 100) / 100.0;
    }

    public double getServings() {
        return servings;
    }

    public int getFoodId() {
        return foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getServingSize() {
        return servingSize;
    }

    public double getCaloriesPerServing() {
        return caloriesPerServing;
    }

    public double getProteinPerServing() {
        return proteinPerServing;
    }

    public double getCarbohydratesPerServing() {
        return carbohydratesPerServing;
    }

    public double getFatPerServing() {
        return fatPerServing;
    }

    @Override
    public double getCalories() {
        return caloriesPerServing * servings;
    }

    @Override
    public double getProtein() {
        return proteinPerServing * servings;
    }

    @Override
    public double getCarbohydrates() {
        return carbohydratesPerServing * servings;
    }

    @Override
    public double getFat() {
        return fatPerServing * servings;
    }
}
