package com.nutriplanner.model;

/**
 * A user's estimated daily needs: BMR, TDEE, the calorie target for their
 * goal and the matching protein, carbohydrate and fat targets in grams.
 *
 * It implements NutritionalValue, so targets and actual intake can be
 * compared with the same methods.
 */
public class NutritionTargets implements NutritionalValue {

    private final double bmr;
    private final double tdee;
    private final HealthGoal goal;
    private final int calorieTarget;
    private final boolean minimumApplied;
    private final int minimumCalories;
    private final int proteinGrams;
    private final int carbohydrateGrams;
    private final int fatGrams;

    public NutritionTargets(double bmr, double tdee, HealthGoal goal, int calorieTarget,
                            boolean minimumApplied, int minimumCalories,
                            int proteinGrams, int carbohydrateGrams, int fatGrams) {
        this.bmr = bmr;
        this.tdee = tdee;
        this.goal = goal;
        this.calorieTarget = calorieTarget;
        this.minimumApplied = minimumApplied;
        this.minimumCalories = minimumCalories;
        this.proteinGrams = proteinGrams;
        this.carbohydrateGrams = carbohydrateGrams;
        this.fatGrams = fatGrams;
    }

    /** Basal Metabolic Rate: energy used at complete rest. */
    public double getBmr() {
        return bmr;
    }

    /** Total Daily Energy Expenditure: energy used on a typical day. */
    public double getTdee() {
        return tdee;
    }

    public HealthGoal getGoal() {
        return goal;
    }

    /** The suggested number of kcal to eat each day. */
    public int getCalorieTarget() {
        return calorieTarget;
    }

    /** True when the weight-loss target was raised to the safe minimum. */
    public boolean isMinimumApplied() {
        return minimumApplied;
    }

    public int getMinimumCalories() {
        return minimumCalories;
    }

    public int getProteinGrams() {
        return proteinGrams;
    }

    public int getCarbohydrateGrams() {
        return carbohydrateGrams;
    }

    public int getFatGrams() {
        return fatGrams;
    }

    @Override
    public double getCalories() {
        return calorieTarget;
    }

    @Override
    public double getProtein() {
        return proteinGrams;
    }

    @Override
    public double getCarbohydrates() {
        return carbohydrateGrams;
    }

    @Override
    public double getFat() {
        return fatGrams;
    }
}
