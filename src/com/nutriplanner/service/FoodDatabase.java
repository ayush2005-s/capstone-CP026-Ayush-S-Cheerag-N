package com.nutriplanner.service;

import com.nutriplanner.exception.DataStorageException;
import com.nutriplanner.exception.ValidationException;
import com.nutriplanner.model.CustomFood;
import com.nutriplanner.model.DietType;
import com.nutriplanner.model.Food;
import com.nutriplanner.model.FoodCategory;
import com.nutriplanner.storage.DataStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * The food database: the built-in sample foods plus foods added by users.
 *
 * Foods are kept in a TreeMap keyed by ID, so lists always come out in ID order.
 * Built-in foods use IDs 1 to 999; foods added by users start at 1001.
 */
public final class FoodDatabase {

    public static final int FIRST_CUSTOM_ID = 1001;
    public static final int MIN_SEARCH_LENGTH = 2;

    private final DataStorage storage;
    private final Map<Integer, Food> foods = new TreeMap<>();

    public FoodDatabase(DataStorage storage) {
        this.storage = storage;
        for (Food food : SampleFoods.create()) {
            foods.put(food.getId(), food);
        }
        for (CustomFood food : storage.loadCustomFoods()) {
            // A custom food may not replace a built-in one or a food with the same name.
            if (food.getId() >= FIRST_CUSTOM_ID && !foods.containsKey(food.getId())
                    && !findByName(food.getName()).isPresent()) {
                foods.put(food.getId(), food);
            }
        }
    }

    /** Every food, in ID order. */
    public List<Food> getAllFoods() {
        return new ArrayList<>(foods.values());
    }

    public Optional<Food> findById(int id) {
        return Optional.ofNullable(foods.get(id));
    }

    /** Finds a food by its exact name, ignoring upper and lower case. */
    public Optional<Food> findByName(String name) {
        String wanted = name == null ? "" : name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        for (Food food : foods.values()) {
            if (food.getName().toLowerCase(Locale.ROOT).equals(wanted)) {
                return Optional.of(food);
            }
        }
        return Optional.empty();
    }

    /**
     * Searches food names. When no name matches, it searches category names
     * instead, so "fruit" lists all fruits. Foods whose name starts with the
     * search text are listed first, then the other matches, alphabetically.
     *
     * @throws ValidationException when the search text is shorter than 2 characters
     */
    public List<Food> search(String text) {
        String query = text == null ? "" : text.trim();
        if (query.length() < MIN_SEARCH_LENGTH) {
            throw new ValidationException("Please type at least " + MIN_SEARCH_LENGTH + " letters to search.");
        }
        String lower = query.toLowerCase(Locale.ROOT);
        List<Food> results = new ArrayList<>();
        for (Food food : foods.values()) {
            if (food.nameContains(query)) {
                results.add(food);
            }
        }
        if (results.isEmpty()) {
            for (Food food : foods.values()) {
                if (food.categoryContains(query)) {
                    results.add(food);
                }
            }
        }
        results.sort(Comparator
                .comparing((Food f) -> !f.getName().toLowerCase(Locale.ROOT).startsWith(lower))
                .thenComparing(f -> f.getName().toLowerCase(Locale.ROOT)));
        return results;
    }

    public List<Food> getByCategory(FoodCategory category) {
        List<Food> results = new ArrayList<>();
        for (Food food : foods.values()) {
            if (food.getCategory() == category) {
                results.add(food);
            }
        }
        return results;
    }

    /** Foods that a person with the given dietary preference can eat. */
    public List<Food> getSuitableFoods(DietType preference) {
        List<Food> results = new ArrayList<>();
        for (Food food : foods.values()) {
            if (preference.allows(food.getDietType())) {
                results.add(food);
            }
        }
        return results;
    }

    /**
     * Adds a new food, gives it the next free ID and saves it.
     *
     * @throws ValidationException  when a value is not valid or the name already exists
     * @throws DataStorageException when the food cannot be saved
     */
    public CustomFood addCustomFood(String name, FoodCategory category, String servingSize, double calories,
                                    double protein, double carbohydrates, double fat, DietType dietType,
                                    String addedBy) {
        Optional<Food> existing = findByName(name);
        if (existing.isPresent()) {
            throw new ValidationException("A food called '" + existing.get().getName()
                    + "' already exists (ID " + existing.get().getId() + ").");
        }
        CustomFood food = new CustomFood(nextCustomId(), name, category, servingSize, calories,
                protein, carbohydrates, fat, dietType, addedBy);
        foods.put(food.getId(), food);
        try {
            storage.saveCustomFoods(getCustomFoods());
        } catch (DataStorageException e) {
            foods.remove(food.getId());
            throw e;
        }
        return food;
    }

    /** Only the foods that users added. */
    public List<CustomFood> getCustomFoods() {
        List<CustomFood> custom = new ArrayList<>();
        for (Food food : foods.values()) {
            if (food instanceof CustomFood) {
                custom.add((CustomFood) food);
            }
        }
        return custom;
    }

    public int size() {
        return foods.size();
    }

    private int nextCustomId() {
        int next = FIRST_CUSTOM_ID;
        for (int id : foods.keySet()) {
            if (id >= next) {
                next = id + 1;
            }
        }
        return next;
    }
}
