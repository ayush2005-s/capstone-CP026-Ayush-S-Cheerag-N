package com.nutriplanner.model;

/**
 * The user's health goal. Each goal changes the daily calorie target and
 * the way calories are split between protein, carbohydrates and fat.
 *
 * A difference of about 500 kcal a day changes body weight by roughly
 * 0.5 kg a week (1 kg of body fat stores about 7,700 kcal).
 */
public enum HealthGoal implements Labeled {

    WEIGHT_LOSS("Weight Loss", "eat about 500 kcal less than you burn", -500, 30, 40, 30),
    WEIGHT_MAINTENANCE("Weight Maintenance", "eat about what you burn", 0, 20, 50, 30),
    WEIGHT_GAIN("Weight Gain", "eat about 500 kcal more than you burn", 500, 25, 50, 25);

    private final String label;
    private final String description;
    private final int calorieAdjustment;
    private final int proteinPercent;
    private final int carbohydratePercent;
    private final int fatPercent;

    HealthGoal(String label, String description, int calorieAdjustment,
               int proteinPercent, int carbohydratePercent, int fatPercent) {
        this.label = label;
        this.description = description;
        this.calorieAdjustment = calorieAdjustment;
        this.proteinPercent = proteinPercent;
        this.carbohydratePercent = carbohydratePercent;
        this.fatPercent = fatPercent;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /** Calories added to (or removed from) the daily energy expenditure. */
    public int getCalorieAdjustment() {
        return calorieAdjustment;
    }

    public int getProteinPercent() {
        return proteinPercent;
    }

    public int getCarbohydratePercent() {
        return carbohydratePercent;
    }

    public int getFatPercent() {
        return fatPercent;
    }
}
