package com.nutriplanner.service;

import com.nutriplanner.exception.DataStorageException;
import com.nutriplanner.model.DailyMealPlan;
import com.nutriplanner.model.Food;
import com.nutriplanner.model.Meal;
import com.nutriplanner.model.MealEntry;
import com.nutriplanner.model.MealType;
import com.nutriplanner.storage.DataStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stores every user's meals, day by day, and saves them after each change.
 *
 * Structure: username -> (date -> DailyMealPlan). The inner TreeMap keeps the
 * days in date order.
 *
 * Every change is saved straight away. If saving fails, the change is undone,
 * so what the user sees always matches what is in the file.
 */
public class MealPlanner {

    private final DataStorage storage;
    private final Map<String, TreeMap<LocalDate, DailyMealPlan>> plansByUser = new HashMap<>();

    public MealPlanner(DataStorage storage) {
        this.storage = storage;
        for (DailyMealPlan plan : storage.loadMealPlans()) {
            plansFor(plan.getUsername()).put(plan.getDate(), plan);
        }
    }

    private TreeMap<LocalDate, DailyMealPlan> plansFor(String username) {
        return plansByUser.computeIfAbsent(UserManager.normalizeUsername(username), key -> new TreeMap<>());
    }

    /** Returns the plan for a day, creating an empty one if needed. */
    public DailyMealPlan getPlan(String username, LocalDate date) {
        return plansFor(username).computeIfAbsent(date, d -> new DailyMealPlan(UserManager.normalizeUsername(username), d));
    }

    /** One plan for each day from 'from' to 'to' (inclusive), including empty days. */
    public List<DailyMealPlan> getPlansBetween(String username, LocalDate from, LocalDate to) {
        List<DailyMealPlan> result = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            DailyMealPlan plan = plansFor(username).get(day);
            result.add(plan != null ? plan : new DailyMealPlan(UserManager.normalizeUsername(username), day));
        }
        return result;
    }

    /** Adds servings of a food to a meal and saves. */
    public MealEntry addFood(String username, LocalDate date, MealType mealType, Food food, double servings) {
        MealEntry entry = MealEntry.fromFood(food, servings);
        Meal meal = getPlan(username, date).getMeal(mealType);
        meal.addEntry(entry);
        try {
            save();
        } catch (DataStorageException e) {
            meal.removeEntry(meal.size() - 1);
            throw e;
        }
        return entry;
    }

    /** Changes the number of servings of an entry (index starts at 0) and saves. */
    public MealEntry updateServings(String username, LocalDate date, MealType mealType, int index, double servings) {
        MealEntry entry = getPlan(username, date).getMeal(mealType).getEntry(index);
        double oldServings = entry.getServings();
        entry.setServings(servings);
        try {
            save();
        } catch (DataStorageException e) {
            entry.setServings(oldServings);
            throw e;
        }
        return entry;
    }

    /** Removes an entry (index starts at 0) and saves. */
    public MealEntry removeFood(String username, LocalDate date, MealType mealType, int index) {
        Meal meal = getPlan(username, date).getMeal(mealType);
        MealEntry removed = meal.removeEntry(index);
        try {
            save();
        } catch (DataStorageException e) {
            meal.insertEntry(index, removed);
            throw e;
        }
        return removed;
    }

    /** Removes every food from a meal and saves. Returns how many foods were removed. */
    public int clearMeal(String username, LocalDate date, MealType mealType) {
        Meal meal = getPlan(username, date).getMeal(mealType);
        List<MealEntry> removed = meal.clear();
        try {
            save();
        } catch (DataStorageException e) {
            for (MealEntry entry : removed) {
                meal.addEntry(entry);
            }
            throw e;
        }
        return removed.size();
    }

    /** Writes every non-empty plan of every user to meal_plans.csv. */
    private void save() {
        List<DailyMealPlan> all = new ArrayList<>();
        for (TreeMap<LocalDate, DailyMealPlan> plans : plansByUser.values()) {
            for (DailyMealPlan plan : plans.values()) {
                if (!plan.isEmpty()) {
                    all.add(plan);
                }
            }
        }
        storage.saveMealPlans(all);
    }
}
