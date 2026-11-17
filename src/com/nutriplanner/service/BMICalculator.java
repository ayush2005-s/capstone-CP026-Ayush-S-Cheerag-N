package com.nutriplanner.service;

import com.nutriplanner.exception.ValidationException;
import com.nutriplanner.model.BmiCategory;
import com.nutriplanner.model.BmiResult;

/**
 * Calculates Body Mass Index (BMI).
 *
 * Formula: BMI = weight (kg) / height (m) squared
 * Example: 65 kg and 170 cm -> 65 / (1.70 x 1.70) = 22.5
 */
public final class BMICalculator {

    /** WHO healthy BMI range for adults: 18.5 to 24.9. */
    public static final double HEALTHY_BMI_MIN = 18.5;
    public static final double HEALTHY_BMI_MAX = 24.9;

    private BMICalculator() {
        // Utility class: the formulas need no stored state.
    }

    /** Returns the exact BMI for a weight in kilograms and a height in centimetres. */
    public static double calculateBmi(double weightKg, double heightCm) {
        if (Double.isNaN(weightKg) || Double.isNaN(heightCm) || weightKg <= 0 || heightCm <= 0) {
            throw new ValidationException("Height and weight must both be greater than zero.");
        }
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    /** The weight that gives a certain BMI at a certain height. */
    public static double weightForBmi(double bmi, double heightCm) {
        double heightM = heightCm / 100.0;
        return bmi * heightM * heightM;
    }

    /**
     * Calculates BMI, finds its category and the healthy weight range.
     * The BMI is rounded to one decimal first, so the category always
     * matches the number the user sees.
     */
    public static BmiResult evaluate(double weightKg, double heightCm) {
        double bmi = Math.round(calculateBmi(weightKg, heightCm) * 10) / 10.0;
        return new BmiResult(heightCm, weightKg, bmi, BmiCategory.of(bmi),
                weightForBmi(HEALTHY_BMI_MIN, heightCm), weightForBmi(HEALTHY_BMI_MAX, heightCm));
    }
}
