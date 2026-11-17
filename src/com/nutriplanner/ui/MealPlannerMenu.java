package com.nutriplanner.ui;

import com.nutriplanner.model.DailyMealPlan;
import com.nutriplanner.model.DietType;
import com.nutriplanner.model.Food;
import com.nutriplanner.model.Meal;
import com.nutriplanner.model.MealEntry;
import com.nutriplanner.model.MealType;
import com.nutriplanner.model.NutritionTargets;
import com.nutriplanner.model.User;
import com.nutriplanner.service.MealPlanner;
import com.nutriplanner.service.NutritionCalculator;
import com.nutriplanner.util.Fmt;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Plan or record the four meals of a day: add foods with serving quantities,
 * change or remove them, and see meal-wise and daily totals.
 */
public class MealPlannerMenu extends Menu {

    private final User user;
    private final MealPlanner planner;
    private LocalDate date;

    public MealPlannerMenu(AppContext context, User user) {
        super(context);
        this.user = user;
        this.planner = context.getMealPlanner();
        this.date = context.today();
    }

    @Override
    protected String getTitle() {
        return "MEAL PLANNER";
    }

    @Override
    protected void printContext() {
        DailyMealPlan plan = currentPlan();
        NutritionTargets targets = NutritionCalculator.calculateTargets(user.getProfile());
        view.println("Date: " + Fmt.date(date) + dayLabel());
        StringBuilder meals = new StringBuilder();
        for (Meal meal : plan.getMeals()) {
            meals.append(meals.length() == 0 ? "" : " | ")
                    .append(meal.getType().getLabel()).append(" ").append(Fmt.kcal(meal.getCalories()));
        }
        view.println(meals.toString());
        view.println("Day total: " + Fmt.kcal(plan.getCalories()) + " of " + Fmt.kcal(targets.getCalorieTarget())
                + " kcal target");
        view.blankLine();
    }

    @Override
    protected String[] getOptions() {
        return new String[] {
            "Add food to a meal",
            "View meals and nutritional totals",
            "Change the servings of a food",
            "Remove a food from a meal",
            "Clear a whole meal",
            "Choose another date",
            "Back to main menu"
        };
    }

    @Override
    protected boolean handleChoice(int choice) {
        switch (choice) {
            case 1:
                addFood();
                return true;
            case 2:
                view.section("MEALS FOR " + Fmt.date(date));
                view.printBlock(context.getReport().renderMealDetails(currentPlan()));
                input.pause();
                return true;
            case 3:
                changeServings();
                return true;
            case 4:
                removeFood();
                return true;
            case 5:
                clearMeal();
                return true;
            case 6:
                view.blankLine();
                date = input.readDate("Enter a date (DD-MM-YYYY), or today, yesterday, tomorrow [today]: ",
                        context.today());
                view.success("Now planning meals for " + Fmt.date(date) + ".");
                return true;
            default:
                return false;
        }
    }

    private DailyMealPlan currentPlan() {
        return planner.getPlan(user.getUsername(), date);
    }

    private String dayLabel() {
        LocalDate today = context.today();
        if (date.equals(today)) {
            return " (today)";
        }
        if (date.equals(today.plusDays(1))) {
            return " (tomorrow)";
        }
        if (date.equals(today.minusDays(1))) {
            return " (yesterday)";
        }
        return "";
    }

    private MealType chooseMeal() {
        view.blankLine();
        return input.chooseOption("Which meal", MealType.values(), null);
    }

    private void addFood() {
        MealType mealType = chooseMeal();
        FoodPicker picker = new FoodPicker(context);
        boolean addMore = true;
        while (addMore) {
            view.blankLine();
            view.println("Adding to " + mealType.getLabel() + " on " + Fmt.date(date) + ".");
            Food food = picker.pick();
            if (food == null) {
                return;
            }
            DietType diet = user.getProfile().getDietType();
            if (!diet.allows(food.getDietType())) {
                view.info(food.getName() + " is " + food.getDietType().getLabel().toLowerCase(Locale.ROOT)
                        + ", which does not match your " + diet.getLabel().toLowerCase(Locale.ROOT) + " preference.");
                if (!input.readYesNo("Add it anyway?")) {
                    addMore = input.readYesNo("Add a different food to " + mealType.getLabel() + "?");
                    continue;
                }
            }
            double servings = input.readDouble("Number of servings of " + food.getServingSize()
                    + " (for example 1, 2 or 0.5): ", MealEntry.MIN_SERVINGS, MealEntry.MAX_SERVINGS);
            MealEntry entry = planner.addFood(user.getUsername(), date, mealType, food, servings);
            Meal meal = currentPlan().getMeal(mealType);
            view.success("Added " + Fmt.number(entry.getServings()) + " x " + entry.getFoodName() + " to "
                    + mealType.getLabel() + " (" + Fmt.kcal(entry.getCalories()) + " kcal). "
                    + mealType.getLabel() + " now has " + Fmt.kcal(meal.getCalories()) + " kcal.");
            addMore = input.readYesNo("Add another food to " + mealType.getLabel() + "?");
        }
    }

    /**
     * Shows the foods of a meal and asks which one to use.
     * Returns the position starting at 0, or -1 if the meal is empty or the user chose 0.
     */
    private int chooseEntry(MealType mealType, String action) {
        Meal meal = currentPlan().getMeal(mealType);
        view.blankLine();
        view.printBlock(context.getReport().renderMeal(meal));
        if (meal.isEmpty()) {
            return -1;
        }
        int number = input.readInt("Number of the food to " + action + " (1-" + meal.size() + "), or 0 to cancel: ",
                0, meal.size());
        return number - 1;
    }

    private void changeServings() {
        MealType mealType = chooseMeal();
        int index = chooseEntry(mealType, "change");
        if (index < 0) {
            return;
        }
        MealEntry entry = currentPlan().getMeal(mealType).getEntry(index);
        double servings = input.readDouble("New number of servings of " + entry.getServingSize() + " (currently "
                + Fmt.number(entry.getServings()) + "): ", MealEntry.MIN_SERVINGS, MealEntry.MAX_SERVINGS);
        planner.updateServings(user.getUsername(), date, mealType, index, servings);
        view.success(entry.getFoodName() + " is now " + Fmt.number(entry.getServings()) + " serving(s), "
                + Fmt.kcal(entry.getCalories()) + " kcal.");
    }

    private void removeFood() {
        MealType mealType = chooseMeal();
        int index = chooseEntry(mealType, "remove");
        if (index < 0) {
            return;
        }
        MealEntry entry = currentPlan().getMeal(mealType).getEntry(index);
        if (input.readYesNo("Remove " + entry.getFoodName() + " from " + mealType.getLabel() + "?")) {
            planner.removeFood(user.getUsername(), date, mealType, index);
            view.success(entry.getFoodName() + " was removed from " + mealType.getLabel() + ".");
        }
    }

    private void clearMeal() {
        MealType mealType = chooseMeal();
        Meal meal = currentPlan().getMeal(mealType);
        if (meal.isEmpty()) {
            view.println(mealType.getLabel() + " is already empty.");
            return;
        }
        if (input.readYesNo("Remove all " + meal.size() + " food(s) from " + mealType.getLabel() + "?")) {
            int removed = planner.clearMeal(user.getUsername(), date, mealType);
            view.success(removed + " food(s) removed from " + mealType.getLabel() + ".");
        }
    }
}
