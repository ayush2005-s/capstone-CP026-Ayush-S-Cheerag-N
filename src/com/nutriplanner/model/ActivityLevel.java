package com.nutriplanner.model;

/**
 * How active the user is. Each level has a multiplier that turns
 * BMR (energy at rest) into TDEE (energy burned on a typical day).
 */
public enum ActivityLevel implements Labeled {

    SEDENTARY("Sedentary", "little or no exercise", 1.2),
    LIGHTLY_ACTIVE("Lightly active", "light exercise 1-3 days a week", 1.375),
    MODERATELY_ACTIVE("Moderately active", "moderate exercise 3-5 days a week", 1.55),
    VERY_ACTIVE("Very active", "hard exercise 6-7 days a week", 1.725),
    EXTRA_ACTIVE("Extra active", "very hard exercise or a physical job", 1.9);

    private final String label;
    private final String description;
    private final double multiplier;

    ActivityLevel(String label, String description, double multiplier) {
        this.label = label;
        this.description = description;
        this.multiplier = multiplier;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
