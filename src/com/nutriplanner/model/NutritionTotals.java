package com.nutriplanner.model;

import java.util.Collection;

/**
 * An immutable sum of calories, protein, carbohydrates and fat.
 */
public final class NutritionTotals implements NutritionalValue {

    public static final NutritionTotals ZERO = new NutritionTotals(0, 0, 0, 0);

    private final double calories;
    private final double protein;
    private final double carbohydrates;
    private final double fat;

    public NutritionTotals(double calories, double protein, double carbohydrates, double fat) {
        this.calories = calories;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
    }

    /**
     * Adds up any collection of nutritional values. Because it accepts the
     * interface type, it works for foods, meal entries, meals and days alike.
     */
    public static NutritionTotals of(Collection<? extends NutritionalValue> items) {
        NutritionTotals total = ZERO;
        for (NutritionalValue item : items) {
            total = total.plus(item);
        }
        return total;
    }

    /** Returns a new total that also includes the given value. */
    public NutritionTotals plus(NutritionalValue other) {
        return new NutritionTotals(
                calories + other.getCalories(),
                protein + other.getProtein(),
                carbohydrates + other.getCarbohydrates(),
                fat + other.getFat());
    }

    @Override
    public double getCalories() {
        return calories;
    }

    @Override
    public double getProtein() {
        return protein;
    }

    @Override
    public double getCarbohydrates() {
        return carbohydrates;
    }

    @Override
    public double getFat() {
        return fat;
    }
}
