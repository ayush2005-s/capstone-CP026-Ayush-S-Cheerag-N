package com.nutriplanner.model;

/**
 * Gender, used by the Mifflin-St Jeor BMR formula.
 *
 * The formula adds +5 kcal for men and -161 kcal for women. For people who
 * prefer not to say, NutriPlanner uses the average of the two (-78 kcal).
 */
public enum Gender implements Labeled {

    MALE("Male", 5),
    FEMALE("Female", -161),
    OTHER("Other / prefer not to say", -78);

    private final String label;
    private final int bmrConstant;

    Gender(String label, int bmrConstant) {
        this.label = label;
        this.bmrConstant = bmrConstant;
    }

    @Override
    public String getLabel() {
        return label;
    }

    /** The constant added at the end of the Mifflin-St Jeor equation. */
    public int getBmrConstant() {
        return bmrConstant;
    }
}
