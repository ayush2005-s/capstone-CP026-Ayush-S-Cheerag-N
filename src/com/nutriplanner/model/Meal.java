package com.nutriplanner.model;

import com.nutriplanner.exception.ValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One meal of the day (breakfast, lunch, evening snacks or dinner)
 * and the foods in it. Its nutrition is the sum of its entries.
 */
public class Meal implements NutritionalValue {

    private final MealType type;
    private final List<MealEntry> entries = new ArrayList<>();

    public Meal(MealType type) {
        this.type = type;
    }

    public MealType getType() {
        return type;
    }

    /** The foods in this meal. The list cannot be changed from outside. */
    public List<MealEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void addEntry(MealEntry entry) {
        entries.add(entry);
    }

    /** Puts an entry back at a given position (used to undo a removal). */
    public void insertEntry(int index, MealEntry entry) {
        entries.add(Math.max(0, Math.min(index, entries.size())), entry);
    }

    /**
     * Returns the entry at a position. Positions start at 0.
     *
     * @throws ValidationException when the position does not exist
     */
    public MealEntry getEntry(int index) {
        checkIndex(index);
        return entries.get(index);
    }

    /** Removes and returns the entry at a position (starting at 0). */
    public MealEntry removeEntry(int index) {
        checkIndex(index);
        return entries.remove(index);
    }

    /** Removes every entry and returns the entries that were removed. */
    public List<MealEntry> clear() {
        List<MealEntry> removed = new ArrayList<>(entries);
        entries.clear();
        return removed;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= entries.size()) {
            throw new ValidationException(type.getLabel() + " has no item number " + (index + 1) + ".");
        }
    }

    private NutritionTotals totals() {
        return NutritionTotals.of(entries);
    }

    @Override
    public double getCalories() {
        return totals().getCalories();
    }

    @Override
    public double getProtein() {
        return totals().getProtein();
    }

    @Override
    public double getCarbohydrates() {
        return totals().getCarbohydrates();
    }

    @Override
    public double getFat() {
        return totals().getFat();
    }
}
