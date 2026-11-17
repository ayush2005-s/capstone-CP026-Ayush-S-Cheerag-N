package com.nutriplanner.model;

import com.nutriplanner.exception.ValidationException;

import java.util.Locale;

/**
 * A food item in the database. All nutrition values are per ONE serving,
 * and the serving is described in words, for example "1 piece (40 g)".
 *
 * The fields are private and set only through the constructor, which checks
 * every value (encapsulation). A Food cannot be changed after it is created.
 */
public class Food implements NutritionalValue {

    public static final int MAX_NAME_LENGTH = 40;
    public static final int MAX_SERVING_LENGTH = 30;
    public static final double MAX_CALORIES = 3000;
    public static final double MAX_GRAMS = 300;

    private final int id;
    private final String name;
    private final FoodCategory category;
    private final String servingSize;
    private final double calories;
    private final double protein;
    private final double carbohydrates;
    private final double fat;
    private final DietType dietType;

    public Food(int id, String name, FoodCategory category, String servingSize,
                double calories, double protein, double carbohydrates, double fat, DietType dietType) {
        if (id <= 0) {
            throw new ValidationException("Food ID must be a positive number.");
        }
        this.id = id;
        this.name = validateName(name);
        this.servingSize = validateServingSize(servingSize);
        this.category = requireValue(category, "Please choose a food category.");
        this.dietType = requireValue(dietType, "Please choose a diet type.");
        this.calories = checkAmount(calories, MAX_CALORIES, "Calories");
        this.protein = checkAmount(protein, MAX_GRAMS, "Protein");
        this.carbohydrates = checkAmount(carbohydrates, MAX_GRAMS, "Carbohydrates");
        this.fat = checkAmount(fat, MAX_GRAMS, "Fat");
    }

    /** Checks a food name and returns it with extra spaces removed. */
    public static String validateName(String name) {
        String cleaned = name == null ? "" : name.trim().replaceAll("\\s+", " ");
        if (cleaned.length() < 2) {
            throw new ValidationException("Food name must have at least 2 characters.");
        }
        if (cleaned.length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Food name can have at most " + MAX_NAME_LENGTH + " characters.");
        }
        return cleaned;
    }

    /** Checks a serving size description such as "1 bowl (150 g)". */
    public static String validateServingSize(String servingSize) {
        String cleaned = servingSize == null ? "" : servingSize.trim().replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) {
            throw new ValidationException("Serving size cannot be empty, for example '1 bowl (150 g)'.");
        }
        if (cleaned.length() > MAX_SERVING_LENGTH) {
            throw new ValidationException("Serving size can have at most " + MAX_SERVING_LENGTH + " characters.");
        }
        return cleaned;
    }

    private static double checkAmount(double value, double max, String what) {
        if (Double.isNaN(value) || value < 0) {
            throw new ValidationException(what + " cannot be negative.");
        }
        if (value > max) {
            throw new ValidationException(what + " per serving cannot be more than " + (int) max + ".");
        }
        return value;
    }

    private static <T> T requireValue(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }

    /**
     * Returns true when a word in the food's name starts with the search text.
     * The search ignores upper and lower case, so "DAL" finds "Moong dal" and
     * "chap" finds "Chapati", but "tea" does not find "Steamed rice".
     */
    public boolean nameContains(String searchText) {
        String lowerName = name.toLowerCase(Locale.ROOT);
        String query = normalize(searchText);
        int position = lowerName.indexOf(query);
        while (position >= 0) {
            boolean startsWord = position == 0 || !Character.isLetterOrDigit(lowerName.charAt(position - 1));
            if (startsWord) {
                return true;
            }
            position = lowerName.indexOf(query, position + 1);
        }
        return false;
    }

    /** Returns true when the search text appears in the category name, for example "fruit". */
    public boolean categoryContains(String searchText) {
        return category.getLabel().toLowerCase(Locale.ROOT).contains(normalize(searchText));
    }

    private static String normalize(String text) {
        return text.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    /** Built-in foods come with the program; see {@link CustomFood} for user-added foods. */
    public boolean isBuiltIn() {
        return true;
    }

    /** Where the nutrition values came from. Overridden by {@link CustomFood}. */
    public String getSourceLabel() {
        return "NutriPlanner sample data (approximate values)";
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public FoodCategory getCategory() {
        return category;
    }

    public String getServingSize() {
        return servingSize;
    }

    public DietType getDietType() {
        return dietType;
    }

    @Override
    public double getCalories() {
        return calories;
    }

    @Override
    public double getProtein() {
        return protein;
    }

    @Override
    public double getCarbohydrates() {
        return carbohydrates;
    }

    @Override
    public double getFat() {
        return fat;
    }

    @Override
    public String toString() {
        return name + " (" + servingSize + ")";
    }
}
