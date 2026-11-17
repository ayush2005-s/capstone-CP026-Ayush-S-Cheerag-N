package com.nutriplanner.model;

import com.nutriplanner.exception.ValidationException;

/**
 * The personal details of a user that the calculators need.
 *
 * Every setter checks its value and throws a ValidationException when the
 * value is not allowed, so a profile can never hold a negative weight or an
 * impossible age (encapsulation).
 */
public final class UserProfile {

    public static final int MIN_AGE = 15;
    public static final int MAX_AGE = 100;
    public static final double MIN_HEIGHT_CM = 100;
    public static final double MAX_HEIGHT_CM = 250;
    public static final double MIN_WEIGHT_KG = 25;
    public static final double MAX_WEIGHT_KG = 300;
    public static final int MAX_NAME_LENGTH = 50;

    private String name;
    private int age;
    private Gender gender;
    private double heightCm;
    private double weightKg;
    private ActivityLevel activityLevel;
    private DietType dietType;
    private HealthGoal healthGoal;

    public UserProfile(String name, int age, Gender gender, double heightCm, double weightKg,
                       ActivityLevel activityLevel, DietType dietType, HealthGoal healthGoal) {
        setName(name);
        setAge(age);
        setGender(gender);
        setHeightCm(heightCm);
        setWeightKg(weightKg);
        setActivityLevel(activityLevel);
        setDietType(dietType);
        setHealthGoal(healthGoal);
    }

    /** Checks a person's name and returns it with extra spaces removed. */
    public static String validateName(String name) {
        String cleaned = name == null ? "" : name.trim().replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) {
            throw new ValidationException("Name cannot be empty.");
        }
        if (cleaned.length() < 2 || cleaned.length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Name must be between 2 and " + MAX_NAME_LENGTH + " characters long.");
        }
        for (char c : cleaned.toCharArray()) {
            if (!Character.isLetter(c) && c != ' ' && c != '.' && c != '\'' && c != '-') {
                throw new ValidationException("Name can contain only letters, spaces, dots, hyphens and apostrophes.");
            }
        }
        return cleaned;
    }

    public static void validateAge(int age) {
        if (age < MIN_AGE || age > MAX_AGE) {
            throw new ValidationException("Age must be between " + MIN_AGE + " and " + MAX_AGE + " years.");
        }
    }

    public static void validateHeight(double heightCm) {
        if (Double.isNaN(heightCm) || heightCm <= 0) {
            throw new ValidationException("Height must be a positive number.");
        }
        if (heightCm < MIN_HEIGHT_CM || heightCm > MAX_HEIGHT_CM) {
            throw new ValidationException("Height must be between 100 and 250 cm.");
        }
    }

    public static void validateWeight(double weightKg) {
        if (Double.isNaN(weightKg) || weightKg <= 0) {
            throw new ValidationException("Weight must be a positive number.");
        }
        if (weightKg < MIN_WEIGHT_KG || weightKg > MAX_WEIGHT_KG) {
            throw new ValidationException("Weight must be between 25 and 300 kg.");
        }
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = validateName(name);
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        validateAge(age);
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = require(gender, "Please choose a gender.");
    }

    public double getHeightCm() {
        return heightCm;
    }

    /** Height in metres, as used by the BMI formula. */
    public double getHeightInMeters() {
        return heightCm / 100.0;
    }

    public void setHeightCm(double heightCm) {
        validateHeight(heightCm);
        this.heightCm = Math.round(heightCm * 10) / 10.0;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        validateWeight(weightKg);
        this.weightKg = Math.round(weightKg * 10) / 10.0;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = require(activityLevel, "Please choose an activity level.");
    }

    public DietType getDietType() {
        return dietType;
    }

    public void setDietType(DietType dietType) {
        this.dietType = require(dietType, "Please choose a dietary preference.");
    }

    public HealthGoal getHealthGoal() {
        return healthGoal;
    }

    public void setHealthGoal(HealthGoal healthGoal) {
        this.healthGoal = require(healthGoal, "Please choose a health goal.");
    }
}
