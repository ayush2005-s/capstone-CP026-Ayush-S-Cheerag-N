package com.nutriplanner.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * All four meals of one user on one date.
 * An EnumMap keeps the meals in order: breakfast, lunch, evening snacks, dinner.
 */
public class DailyMealPlan implements NutritionalValue {

    private final String username;
    private final LocalDate date;
    private final Map<MealType, Meal> meals = new EnumMap<>(MealType.class);

    public DailyMealPlan(String username, LocalDate date) {
        this.username = username;
        this.date = date;
        for (MealType type : MealType.values()) {
            meals.put(type, new Meal(type));
        }
    }

    public String getUsername() {
        return username;
    }

    public LocalDate getDate() {
        return date;
    }

    public Meal getMeal(MealType type) {
        return meals.get(type);
    }

    /** All four meals in the order they are eaten. */
    public List<Meal> getMeals() {
        return new ArrayList<>(meals.values());
    }

    /** Total number of food entries across all meals. */
    public int getEntryCount() {
        int count = 0;
        for (Meal meal : meals.values()) {
            count += meal.size();
        }
        return count;
    }

    public boolean isEmpty() {
        return getEntryCount() == 0;
    }

    private NutritionTotals totals() {
        return NutritionTotals.of(meals.values());
    }

    @Override
    public double getCalories() {
        return totals().getCalories();
    }

    @Override
    public double getProtein() {
        return totals().getProtein();
    }

    @Override
    public double getCarbohydrates() {
        return totals().getCarbohydrates();
    }

    @Override
    public double getFat() {
        return totals().getFat();
    }
}
