package com.nutriplanner.service;

import com.nutriplanner.model.HealthGoal;
import com.nutriplanner.model.NutritionTargets;
import com.nutriplanner.model.UserProfile;

/**
 * Combines the BMR and TDEE formulas with the user's goal to produce
 * daily calorie and macronutrient targets.
 *
 * Steps:
 *  1. BMR  = Mifflin-St Jeor equation (CalorieCalculator)
 *  2. TDEE = BMR x activity multiplier
 *  3. Target = TDEE + goal adjustment (-500, 0 or +500 kcal)
 *     For weight loss the target never goes below 1,200 kcal (women) or
 *     1,500 kcal (men), and never above TDEE.
 *  4. Macros: the target is split by the goal's percentages.
 *     Protein and carbohydrates give 4 kcal per gram, fat gives 9 kcal per gram.
 */
public final class NutritionCalculator {

    public static final int KCAL_PER_GRAM_PROTEIN = 4;
    public static final int KCAL_PER_GRAM_CARBOHYDRATE = 4;
    public static final int KCAL_PER_GRAM_FAT = 9;

    private NutritionCalculator() {
        // Utility class: the formulas need no stored state.
    }

    /** Calculates all daily targets for a profile. */
    public static NutritionTargets calculateTargets(UserProfile profile) {
        double bmr = CalorieCalculator.calculateBmr(profile.getGender(), profile.getWeightKg(),
                profile.getHeightCm(), profile.getAge());
        double tdee = CalorieCalculator.calculateTdee(bmr, profile.getActivityLevel());
        HealthGoal goal = profile.getHealthGoal();

        int target = (int) Math.round(tdee + goal.getCalorieAdjustment());
        int minimum = CalorieCalculator.minimumCalories(profile.getGender());
        boolean minimumApplied = false;
        if (goal == HealthGoal.WEIGHT_LOSS && target < minimum) {
            // Never suggest more than the person burns, even when TDEE is below the floor.
            target = (int) Math.min(Math.round(tdee), minimum);
            minimumApplied = true;
        }

        return new NutritionTargets(bmr, tdee, goal, target, minimumApplied, minimum,
                gramsFor(target, goal.getProteinPercent(), KCAL_PER_GRAM_PROTEIN),
                gramsFor(target, goal.getCarbohydratePercent(), KCAL_PER_GRAM_CARBOHYDRATE),
                gramsFor(target, goal.getFatPercent(), KCAL_PER_GRAM_FAT));
    }

    /** Grams of a nutrient that provide the given percentage of the calories. */
    public static int gramsFor(int calories, int percent, int kcalPerGram) {
        return (int) Math.round(calories * percent / 100.0 / kcalPerGram);
    }

    /** Share (0-100) of a food's energy that comes from protein, carbohydrates and fat. */
    public static int[] energySplit(double protein, double carbohydrates, double fat) {
        double p = protein * KCAL_PER_GRAM_PROTEIN;
        double c = carbohydrates * KCAL_PER_GRAM_CARBOHYDRATE;
        double f = fat * KCAL_PER_GRAM_FAT;
        double total = p + c + f;
        if (total <= 0) {
            return new int[] {0, 0, 0};
        }
        int proteinShare = (int) Math.round(p * 100 / total);
        int carbohydrateShare = (int) Math.round(c * 100 / total);
        return new int[] {proteinShare, carbohydrateShare, Math.max(0, 100 - proteinShare - carbohydrateShare)};
    }

    /** Calories implied by the macronutrients (4/4/9 rule), used to sanity-check new foods. */
    public static double caloriesFromMacros(double protein, double carbohydrates, double fat) {
        return protein * KCAL_PER_GRAM_PROTEIN + carbohydrates * KCAL_PER_GRAM_CARBOHYDRATE + fat * KCAL_PER_GRAM_FAT;
    }
}
