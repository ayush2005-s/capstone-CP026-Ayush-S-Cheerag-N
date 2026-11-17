package com.nutriplanner.ui;

import com.nutriplanner.model.Food;
import com.nutriplanner.util.Fmt;
import com.nutriplanner.util.TextTable;
import com.nutriplanner.util.TextTable.Align;

import java.util.List;

/**
 * Prints lists of foods as tables. Used by the food database and the meal planner.
 */
public final class FoodTable {

    private FoodTable() {
        // Utility class: no objects needed.
    }

    /** A table whose first column is the food ID. */
    public static String withIds(List<Food> foods) {
        return build(foods, "ID", true);
    }

    /** A table whose first column is a choice number 1, 2, 3 ... */
    public static String numbered(List<Food> foods) {
        return build(foods, "No.", false);
    }

    private static String build(List<Food> foods, String firstHeader, boolean showIds) {
        TextTable table = new TextTable().gap(1)
                .addColumn(firstHeader, 4, Align.RIGHT)
                .addColumn("Food", 22, Align.LEFT)
                .addColumn("Serving", 20, Align.LEFT)
                .addColumn("kcal", 4, Align.RIGHT)
                .addColumn("Prot", 4, Align.RIGHT)
                .addColumn("Carb", 4, Align.RIGHT)
                .addColumn("Fat", 4, Align.RIGHT)
                .addColumn("Diet", 7, Align.LEFT);
        int number = 1;
        for (Food food : foods) {
            table.addRow(showIds ? String.valueOf(food.getId()) : String.valueOf(number++),
                    food.getName(), food.getServingSize(), Fmt.kcal(food.getCalories()),
                    Fmt.oneDecimal(food.getProtein()), Fmt.oneDecimal(food.getCarbohydrates()),
                    Fmt.oneDecimal(food.getFat()), food.getDietType().getShortLabel());
        }
        return table.render();
    }

    /** The line printed under food tables to explain the columns. */
    public static String legend() {
        return "Per serving. Prot = protein, Carb = carbohydrates, Fat = fat (all in grams).";
    }
}
