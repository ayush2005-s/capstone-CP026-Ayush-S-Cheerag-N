package com.nutriplanner.ui;

import com.nutriplanner.exception.ValidationException;
import com.nutriplanner.model.CustomFood;
import com.nutriplanner.model.DietType;
import com.nutriplanner.model.Food;
import com.nutriplanner.model.FoodCategory;
import com.nutriplanner.model.User;
import com.nutriplanner.service.FoodDatabase;
import com.nutriplanner.service.NutritionCalculator;
import com.nutriplanner.util.Fmt;

import java.util.List;

/**
 * View, search and add foods.
 */
public class FoodDatabaseMenu extends Menu {

    private final User user;
    private final FoodDatabase foodDatabase;

    public FoodDatabaseMenu(AppContext context, User user) {
        super(context);
        this.user = user;
        this.foodDatabase = context.getFoodDatabase();
    }

    @Override
    protected String getTitle() {
        return "FOOD DATABASE";
    }

    @Override
    protected void printContext() {
        view.println(foodDatabase.size() + " foods. Nutrition values are approximate sample values per serving.");
        view.blankLine();
    }

    @Override
    protected String[] getOptions() {
        return new String[] {
            "View all foods",
            "Search for a food",
            "View foods by category",
            "View foods that suit my diet (" + user.getProfile().getDietType().getLabel() + ")",
            "View nutritional information of a food",
            "Add a new food",
            "Back to main menu"
        };
    }

    @Override
    protected boolean handleChoice(int choice) {
        switch (choice) {
            case 1:
                showAllByCategory();
                return true;
            case 2:
                search();
                return true;
            case 3:
                FoodCategory category = input.chooseOption("Categories", FoodCategory.values(), null);
                showList(category.getLabel().toUpperCase(java.util.Locale.ROOT), foodDatabase.getByCategory(category));
                return true;
            case 4:
                DietType diet = user.getProfile().getDietType();
                showList("FOODS FOR A " + diet.getLabel().toUpperCase(java.util.Locale.ROOT) + " DIET",
                        foodDatabase.getSuitableFoods(diet));
                return true;
            case 5:
                showDetails();
                return true;
            case 6:
                addFood();
                return true;
            default:
                return false;
        }
    }

    private void showAllByCategory() {
        view.section("ALL FOODS (" + foodDatabase.size() + ")");
        for (FoodCategory category : FoodCategory.values()) {
            List<Food> foods = foodDatabase.getByCategory(category);
            if (!foods.isEmpty()) {
                view.blankLine();
                view.println(category.getLabel().toUpperCase(java.util.Locale.ROOT));
                view.printBlock(FoodTable.withIds(foods));
            }
        }
        view.blankLine();
        view.println(FoodTable.legend());
        input.pause();
    }

    private void search() {
        view.blankLine();
        String text = input.readLine("Search for (part of a food name, or a category such as 'fruit'): ");
        List<Food> results = foodDatabase.search(text);
        if (results.isEmpty()) {
            view.error("No foods match '" + text + "'. Try a shorter word, such as 'dal', 'rice' or 'egg'.");
            return;
        }
        showList("SEARCH RESULTS: " + results.size() + " food" + (results.size() == 1 ? "" : "s")
                + " match '" + text + "'", results);
    }

    private void showList(String title, List<Food> foods) {
        view.section(title);
        if (foods.isEmpty()) {
            view.println("No foods in this list yet.");
        } else {
            view.printBlock(FoodTable.withIds(foods));
            view.println(FoodTable.legend());
        }
        view.println("Tip: use option 5 with a food ID to see its full nutritional information.");
        input.pause();
    }

    private void showDetails() {
        view.blankLine();
        Food food = new FoodPicker(context).pick();
        if (food == null) {
            return;
        }
        printFoodDetails(food);
        input.pause();
    }

    private void printFoodDetails(Food food) {
        int[] split = NutritionCalculator.energySplit(food.getProtein(), food.getCarbohydrates(), food.getFat());
        view.section("NUTRITIONAL INFORMATION");
        view.field("Food", food.getName());
        view.field("ID", String.valueOf(food.getId()));
        view.field("Category", food.getCategory().getLabel());
        view.field("Serving size", food.getServingSize());
        view.field("Diet type", food.getDietType().getLabel());
        view.field("Source", food.getSourceLabel());
        view.blankLine();
        view.println("Per serving:");
        view.println("  Calories       " + Fmt.padLeft(Fmt.kcal(food.getCalories()), 6) + " kcal");
        view.println("  Protein        " + Fmt.padLeft(Fmt.oneDecimal(food.getProtein()), 6) + " g    ("
                + split[0] + "% of energy)");
        view.println("  Carbohydrates  " + Fmt.padLeft(Fmt.oneDecimal(food.getCarbohydrates()), 6) + " g    ("
                + split[1] + "% of energy)");
        view.println("  Fat            " + Fmt.padLeft(Fmt.oneDecimal(food.getFat()), 6) + " g    ("
                + split[2] + "% of energy)");
        DietType userDiet = user.getProfile().getDietType();
        view.blankLine();
        if (userDiet.allows(food.getDietType())) {
            view.println("This food suits your " + userDiet.getLabel().toLowerCase(java.util.Locale.ROOT) + " diet.");
        } else {
            view.println("This food is " + food.getDietType().getLabel().toLowerCase(java.util.Locale.ROOT)
                    + ", so it does not suit your " + userDiet.getLabel().toLowerCase(java.util.Locale.ROOT) + " diet.");
        }
    }

    private void addFood() {
        view.section("ADD A NEW FOOD");
        view.println("Enter the values for ONE serving. Press Enter on the name to cancel.");
        String name = askFoodName();
        if (name == null) {
            view.println("No food was added.");
            return;
        }
        FoodCategory category = input.chooseOption("Category", FoodCategory.values(), null);
        String serving = askServingSize();
        double calories = input.readDouble("Calories per serving (kcal, 0-" + (int) Food.MAX_CALORIES + "): ",
                0, Food.MAX_CALORIES);
        double protein = input.readDouble("Protein per serving (g, 0-" + (int) Food.MAX_GRAMS + "): ", 0, Food.MAX_GRAMS);
        double carbohydrates = input.readDouble("Carbohydrates per serving (g, 0-" + (int) Food.MAX_GRAMS + "): ",
                0, Food.MAX_GRAMS);
        double fat = input.readDouble("Fat per serving (g, 0-" + (int) Food.MAX_GRAMS + "): ", 0, Food.MAX_GRAMS);
        DietType diet = input.chooseOption("Diet type", DietType.values(), null);

        // Sanity check: protein and carbohydrates give 4 kcal/g and fat 9 kcal/g.
        double fromMacros = NutritionCalculator.caloriesFromMacros(protein, carbohydrates, fat);
        if (Math.abs(fromMacros - calories) > Math.max(30, calories * 0.25)) {
            view.info("Protein, carbohydrates and fat add up to about " + Fmt.kcal(fromMacros)
                    + " kcal, but you entered " + Fmt.kcal(calories) + " kcal. Please check the values.");
        }
        view.blankLine();
        view.printWrapped("", name + ", " + serving + ": " + Fmt.kcal(calories) + " kcal, protein " + Fmt.oneDecimal(protein)
                + " g, carbohydrates " + Fmt.oneDecimal(carbohydrates) + " g, fat " + Fmt.oneDecimal(fat)
                + " g (" + diet.getLabel() + ", " + category.getLabel() + ")");
        if (!input.readYesNo("Save this food?")) {
            view.println("No food was added.");
            return;
        }
        CustomFood food = foodDatabase.addCustomFood(name, category, serving, calories, protein, carbohydrates, fat,
                diet, user.getUsername());
        view.success("Food added with ID " + food.getId() + ". You can now find it in search and the meal planner.");
    }

    private String askFoodName() {
        while (true) {
            String text = input.readLine("Food name: ");
            if (text.isEmpty()) {
                return null;
            }
            try {
                String name = Food.validateName(text);
                if (foodDatabase.findByName(name).isPresent()) {
                    Food existing = foodDatabase.findByName(name).get();
                    view.error("A food called '" + existing.getName() + "' already exists (ID " + existing.getId()
                            + "). Please use a different name.");
                    continue;
                }
                return name;
            } catch (ValidationException e) {
                view.error(e.getMessage());
            }
        }
    }

    private String askServingSize() {
        while (true) {
            String text = input.readLine("Serving size, for example '1 bowl (150 g)': ");
            try {
                return Food.validateServingSize(text);
            } catch (ValidationException e) {
                view.error(e.getMessage());
            }
        }
    }
}
