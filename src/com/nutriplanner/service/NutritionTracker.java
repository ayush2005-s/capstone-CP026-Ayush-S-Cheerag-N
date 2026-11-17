package com.nutriplanner.service;

import com.nutriplanner.model.DailyMealPlan;
import com.nutriplanner.model.DailySummary;
import com.nutriplanner.model.DietType;
import com.nutriplanner.model.Meal;
import com.nutriplanner.model.MealType;
import com.nutriplanner.model.NutritionTargets;
import com.nutriplanner.model.NutritionTotals;
import com.nutriplanner.model.UserProfile;
import com.nutriplanner.util.Fmt;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Compares what a user ate on a day with their daily targets and gives
 * simple, practical feedback.
 */
public class NutritionTracker {

    /** Within 5% of the calorie target counts as "on target". */
    private static final double ON_TARGET_MARGIN = 0.05;

    /** Builds the day's summary: targets, what was eaten and what is left. */
    public DailySummary summarize(UserProfile profile, DailyMealPlan plan) {
        NutritionTargets targets = NutritionCalculator.calculateTargets(profile);
        NutritionTotals consumed = NutritionTotals.of(plan.getMeals());
        Map<MealType, Double> caloriesByMeal = new EnumMap<>(MealType.class);
        for (Meal meal : plan.getMeals()) {
            caloriesByMeal.put(meal.getType(), meal.getCalories());
        }
        return new DailySummary(plan.getDate(), targets, consumed, caloriesByMeal, plan.getEntryCount());
    }

    /** Short advice messages based on the summary and the user's diet. */
    public List<String> buildAdvice(DailySummary summary, DietType diet) {
        List<String> advice = new ArrayList<>();
        if (summary.getEntryCount() == 0) {
            advice.add("No food has been logged for this day yet. Use the Meal Planner (option 6) to add your meals.");
            return advice;
        }

        double target = summary.getTargets().getCalories();
        double eaten = summary.getConsumed().getCalories();
        double remaining = summary.getRemainingCalories();
        if (eaten > target * (1 + ON_TARGET_MARGIN)) {
            advice.add("You are " + Fmt.kcal(-remaining) + " kcal over your daily target.");
        } else if (eaten >= target * (1 - ON_TARGET_MARGIN)) {
            advice.add("You are right on your calorie target for the day. Well done.");
        } else {
            advice.add("You can still eat about " + Fmt.kcal(remaining) + " kcal today.");
        }

        int caloriePercent = Fmt.percent(eaten, target);
        int proteinPercent = Fmt.percent(summary.getConsumed().getProtein(), summary.getTargets().getProtein());
        int carbohydratePercent = Fmt.percent(summary.getConsumed().getCarbohydrates(),
                summary.getTargets().getCarbohydrates());
        int fatPercent = Fmt.percent(summary.getConsumed().getFat(), summary.getTargets().getFat());
        if (proteinPercent + 20 < caloriePercent) {
            advice.add("Protein is low compared with your calories. Good sources for your diet: "
                    + proteinSources(diet) + ".");
        }
        if (carbohydratePercent > caloriePercent + 20) {
            advice.add("Carbohydrates are high compared with your calories. Try a smaller portion of rice or roti"
                    + " and add dal, vegetables or curd.");
        }
        if (fatPercent > caloriePercent + 20) {
            advice.add("Fat is high compared with your calories. Choose fewer fried foods and use less oil or ghee.");
        }
        return advice;
    }

    private static String proteinSources(DietType diet) {
        switch (diet) {
            case VEGAN:
                return "dal, chole, rajma, soya chunks, tofu and peanuts";
            case VEGETARIAN:
                return "dal, paneer, curd, milk, chole and soya chunks";
            default:
                return "eggs, chicken, fish, dal and curd";
        }
    }
}
