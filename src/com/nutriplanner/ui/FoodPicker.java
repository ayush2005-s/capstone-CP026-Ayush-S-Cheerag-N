package com.nutriplanner.ui;

import com.nutriplanner.exception.ValidationException;
import com.nutriplanner.model.Food;
import com.nutriplanner.service.FoodDatabase;

import java.util.List;
import java.util.Optional;

/**
 * Lets the user find a food by typing its ID or part of its name.
 * Used by the food database (to view details) and the meal planner (to add food).
 */
public class FoodPicker {

    private static final int MAX_RESULTS_SHOWN = 20;

    private final ConsoleView view;
    private final ConsoleInput input;
    private final FoodDatabase foodDatabase;

    public FoodPicker(AppContext context) {
        this.view = context.getView();
        this.input = context.getInput();
        this.foodDatabase = context.getFoodDatabase();
    }

    /** Returns the chosen food, or null when the user presses Enter to cancel. */
    public Food pick() {
        while (true) {
            String text = input.readLine("Type a food name to search, or a food ID (Enter to cancel): ");
            if (text.isEmpty()) {
                return null;
            }
            if (text.matches("\\d+")) {
                Optional<Food> byId = text.length() <= 6 ? foodDatabase.findById(Integer.parseInt(text)) : Optional.empty();
                if (byId.isPresent()) {
                    view.println("Selected: " + byId.get().getName() + ", " + byId.get().getServingSize()
                            + ", " + Math.round(byId.get().getCalories()) + " kcal");
                    return byId.get();
                }
                view.error("No food has the ID " + text + ". Search by name instead, or see the food list.");
                continue;
            }
            List<Food> results;
            try {
                results = foodDatabase.search(text);
            } catch (ValidationException e) {
                view.error(e.getMessage());
                continue;
            }
            if (results.isEmpty()) {
                view.error("No foods match '" + text + "'. Try a shorter word, such as 'dal', 'rice' or 'egg'.");
                continue;
            }
            if (results.size() == 1) {
                Food food = results.get(0);
                view.println("Found: " + food.getName() + ", " + food.getServingSize() + ", "
                        + Math.round(food.getCalories()) + " kcal");
                return food;
            }
            Food chosen = chooseFrom(results);
            if (chosen != null) {
                return chosen;
            }
        }
    }

    /** Shows the search results and lets the user pick one; 0 searches again. */
    private Food chooseFrom(List<Food> results) {
        List<Food> shown = results.size() > MAX_RESULTS_SHOWN ? results.subList(0, MAX_RESULTS_SHOWN) : results;
        view.blankLine();
        view.println(results.size() + " foods match:");
        view.printBlock(FoodTable.numbered(shown));
        if (shown.size() < results.size()) {
            view.println("Showing the first " + shown.size() + ". Type more letters to narrow the search.");
        }
        int choice = input.readInt("Choose a food (1-" + shown.size() + "), or 0 to search again: ", 0, shown.size());
        return choice == 0 ? null : shown.get(choice - 1);
    }
}
