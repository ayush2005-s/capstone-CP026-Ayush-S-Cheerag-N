package com.nutriplanner.model;

/**
 * The result of a BMI calculation: the BMI value, its category and the
 * healthy weight range for the person's height.
 */
public class BmiResult {

    private final double heightCm;
    private final double weightKg;
    private final double bmi;
    private final BmiCategory category;
    private final double healthyMinWeightKg;
    private final double healthyMaxWeightKg;

    public BmiResult(double heightCm, double weightKg, double bmi, BmiCategory category,
                     double healthyMinWeightKg, double healthyMaxWeightKg) {
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.bmi = bmi;
        this.category = category;
        this.healthyMinWeightKg = healthyMinWeightKg;
        this.healthyMaxWeightKg = healthyMaxWeightKg;
    }

    public double getHeightCm() {
        return heightCm;
    }

    public double getWeightKg() {
        return weightKg;
    }

    /** BMI rounded to one decimal place. */
    public double getBmi() {
        return bmi;
    }

    public BmiCategory getCategory() {
        return category;
    }

    public double getHealthyMinWeightKg() {
        return healthyMinWeightKg;
    }

    public double getHealthyMaxWeightKg() {
        return healthyMaxWeightKg;
    }
}
