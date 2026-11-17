package com.nutriplanner.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

/**
 * A comparison of what a user ate on one day with their daily targets.
 */
public class DailySummary {

    private final LocalDate date;
    private final NutritionTargets targets;
    private final NutritionTotals consumed;
    private final Map<MealType, Double> caloriesByMeal;
    private final int entryCount;

    public DailySummary(LocalDate date, NutritionTargets targets, NutritionTotals consumed,
                        Map<MealType, Double> caloriesByMeal, int entryCount) {
        this.date = date;
        this.targets = targets;
        this.consumed = consumed;
        this.caloriesByMeal = Collections.unmodifiableMap(caloriesByMeal);
        this.entryCount = entryCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public NutritionTargets getTargets() {
        return targets;
    }

    public NutritionTotals getConsumed() {
        return consumed;
    }

    public Map<MealType, Double> getCaloriesByMeal() {
        return caloriesByMeal;
    }

    public int getEntryCount() {
        return entryCount;
    }

    /** Calories still available today. Negative when the target was exceeded. */
    public double getRemainingCalories() {
        return targets.getCalories() - consumed.getCalories();
    }

    public double getRemainingProtein() {
        return targets.getProtein() - consumed.getProtein();
    }

    public double getRemainingCarbohydrates() {
        return targets.getCarbohydrates() - consumed.getCarbohydrates();
    }

    public double getRemainingFat() {
        return targets.getFat() - consumed.getFat();
    }
}
