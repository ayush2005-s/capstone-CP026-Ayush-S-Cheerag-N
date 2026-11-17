package com.nutriplanner.service;

import com.nutriplanner.exception.ValidationException;
import com.nutriplanner.model.ActivityLevel;
import com.nutriplanner.model.Gender;

/**
 * Calculates energy needs.
 *
 * BMR (Basal Metabolic Rate) uses the Mifflin-St Jeor equation (1990):
 *   BMR = 10 x weight(kg) + 6.25 x height(cm) - 5 x age(years) + s
 *   where s = +5 for men and -161 for women.
 *
 * TDEE (Total Daily Energy Expenditure) = BMR x activity multiplier.
 */
public final class CalorieCalculator {

    /** Common safety floors for weight-loss diets without medical supervision. */
    public static final int MINIMUM_CALORIES_MEN = 1500;
    public static final int MINIMUM_CALORIES_WOMEN = 1200;

    private CalorieCalculator() {
        // Utility class: the formulas need no stored state.
    }

    /** Basal Metabolic Rate in kcal per day (Mifflin-St Jeor). */
    public static double calculateBmr(Gender gender, double weightKg, double heightCm, int age) {
        if (gender == null) {
            throw new ValidationException("Gender is required to calculate BMR.");
        }
        if (weightKg <= 0 || heightCm <= 0 || age <= 0) {
            throw new ValidationException("Weight, height and age must all be greater than zero.");
        }
        return 10 * weightKg + 6.25 * heightCm - 5 * age + gender.getBmrConstant();
    }

    /** Total Daily Energy Expenditure in kcal per day. */
    public static double calculateTdee(double bmr, ActivityLevel activityLevel) {
        if (activityLevel == null) {
            throw new ValidationException("Activity level is required to calculate TDEE.");
        }
        return bmr * activityLevel.getMultiplier();
    }

    /** The lowest daily calorie target NutriPlanner suggests for weight loss. */
    public static int minimumCalories(Gender gender) {
        return gender == Gender.MALE ? MINIMUM_CALORIES_MEN : MINIMUM_CALORIES_WOMEN;
    }
}
