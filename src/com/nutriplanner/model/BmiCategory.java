package com.nutriplanner.model;

/**
 * World Health Organization BMI categories for adults.
 */
public enum BmiCategory implements Labeled {

    UNDERWEIGHT("Underweight", 0, 18.5,
            "Your weight is below the healthy range for your height. Eating enough energy and protein"
            + " is important. If you are losing weight without trying, please see a doctor."),
    NORMAL("Normal weight", 18.5, 25.0,
            "Your weight is in the healthy range for your height. A balanced diet and regular activity"
            + " will help you stay here."),
    OVERWEIGHT("Overweight", 25.0, 30.0,
            "Your weight is above the healthy range for your height. Small, steady changes to portions"
            + " and activity can lower health risks."),
    OBESE("Obese", 30.0, Double.MAX_VALUE,
            "Your weight is well above the healthy range for your height, which raises the risk of"
            + " diabetes and heart disease. A doctor or dietitian can help you plan safe changes.");

    private final String label;
    private final double lowerBound;
    private final double upperBound;
    private final String explanation;

    BmiCategory(String label, double lowerBound, double upperBound, String explanation) {
        this.label = label;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.explanation = explanation;
    }

    @Override
    public String getLabel() {
        return label;
    }

    /** A short explanation of what this category means. */
    public String getExplanation() {
        return explanation;
    }

    /** The BMI range of this category as text, for example "18.5 - 24.9". */
    public String getRangeText() {
        if (this == UNDERWEIGHT) {
            return "below 18.5";
        }
        if (this == OBESE) {
            return "30.0 and above";
        }
        return String.format(java.util.Locale.US, "%.1f - %.1f", lowerBound, upperBound - 0.1);
    }

    /** Finds the category for a BMI value. */
    public static BmiCategory of(double bmi) {
        for (BmiCategory category : values()) {
            if (bmi >= category.lowerBound && bmi < category.upperBound) {
                return category;
            }
        }
        return OBESE;
    }
}
