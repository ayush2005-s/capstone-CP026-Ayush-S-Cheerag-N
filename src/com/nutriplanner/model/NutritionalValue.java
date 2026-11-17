package com.nutriplanner.model;

/**
 * Anything that has calories, protein, carbohydrates and fat.
 *
 * This interface is the main example of polymorphism in NutriPlanner:
 * a Food, a MealEntry, a Meal, a whole DailyMealPlan and the user's
 * NutritionTargets all implement it, so the same code can add them up,
 * compare them and print them.
 */
public interface NutritionalValue {

    /** Energy in kilocalories (kcal). */
    double getCalories();

    /** Protein in grams. */
    double getProtein();

    /** Carbohydrates in grams. */
    double getCarbohydrates();

    /** Fat in grams. */
    double getFat();
}
