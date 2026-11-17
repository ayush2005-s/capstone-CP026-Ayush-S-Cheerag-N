package com.nutriplanner.service;

import com.nutriplanner.model.DietType;
import com.nutriplanner.model.Food;
import com.nutriplanner.model.FoodCategory;

import java.util.ArrayList;
import java.util.List;

import static com.nutriplanner.model.DietType.NON_VEGETARIAN;
import static com.nutriplanner.model.DietType.VEGAN;
import static com.nutriplanner.model.DietType.VEGETARIAN;
import static com.nutriplanner.model.FoodCategory.BEVERAGES;
import static com.nutriplanner.model.FoodCategory.BREAKFAST;
import static com.nutriplanner.model.FoodCategory.DAIRY;
import static com.nutriplanner.model.FoodCategory.DALS_AND_CURRIES;
import static com.nutriplanner.model.FoodCategory.FRUITS;
import static com.nutriplanner.model.FoodCategory.NUTS_AND_FATS;
import static com.nutriplanner.model.FoodCategory.PROTEIN_FOODS;
import static com.nutriplanner.model.FoodCategory.RICE_AND_BREADS;
import static com.nutriplanner.model.FoodCategory.SNACKS;
import static com.nutriplanner.model.FoodCategory.VEGETABLES;

/**
 * The built-in food list.
 *
 * SAMPLE NUTRITIONAL VALUES: all numbers are approximate values per serving for
 * typical home-style cooking, rounded for teaching purposes. They are based on
 * commonly published figures (Indian Food Composition Tables 2017, USDA
 * FoodData Central). Real values change with recipe, oil and portion size.
 *
 * Columns: ID, name, category, serving size, kcal, protein g, carbohydrates g, fat g, diet type.
 */
public final class SampleFoods {

    private SampleFoods() {
        // Utility class: no objects needed.
    }

    public static List<Food> create() {
        List<Food> foods = new ArrayList<>();

        // Breakfast dishes
        add(foods, 1, "Idli", BREAKFAST, "1 piece (40 g)", 58, 2.0, 12.0, 0.2, VEGAN);
        add(foods, 2, "Plain dosa", BREAKFAST, "1 medium (75 g)", 135, 3.5, 22.0, 3.7, VEGAN);
        add(foods, 3, "Masala dosa", BREAKFAST, "1 piece (150 g)", 260, 5.5, 36.0, 10.5, VEGAN);
        add(foods, 4, "Upma", BREAKFAST, "1 bowl (150 g)", 190, 4.5, 28.0, 6.8, VEGAN);
        add(foods, 5, "Poha", BREAKFAST, "1 plate (150 g)", 180, 3.5, 30.0, 5.2, VEGAN);
        add(foods, 6, "Medu vada", BREAKFAST, "1 piece (50 g)", 135, 4.5, 13.0, 7.2, VEGAN);
        add(foods, 7, "Uttapam", BREAKFAST, "1 piece (120 g)", 200, 5.5, 32.0, 5.5, VEGAN);
        add(foods, 8, "Ven pongal", BREAKFAST, "1 bowl (150 g)", 210, 6.0, 30.0, 7.4, VEGETARIAN);
        add(foods, 9, "Aloo paratha", BREAKFAST, "1 piece (100 g)", 260, 5.5, 36.0, 10.3, VEGETARIAN);
        add(foods, 10, "Moong dal chilla", BREAKFAST, "1 piece (80 g)", 120, 6.5, 15.0, 3.8, VEGAN);
        add(foods, 11, "Oats porridge (milk)", BREAKFAST, "1 bowl (40 g oats)", 240, 10.0, 32.0, 8.0, VEGETARIAN);

        // Rice and breads
        add(foods, 12, "Steamed rice", RICE_AND_BREADS, "1 cup cooked (150 g)", 195, 4.0, 43.0, 0.4, VEGAN);
        add(foods, 13, "Brown rice", RICE_AND_BREADS, "1 cup cooked (150 g)", 170, 3.9, 35.0, 1.3, VEGAN);
        add(foods, 14, "Chapati (with oil)", RICE_AND_BREADS, "1 medium (40 g)", 120, 3.1, 18.0, 3.7, VEGAN);
        add(foods, 15, "Roti (phulka, no oil)", RICE_AND_BREADS, "1 piece (30 g)", 85, 2.9, 17.5, 0.6, VEGAN);
        add(foods, 16, "Poori", RICE_AND_BREADS, "1 piece (25 g)", 100, 1.6, 11.0, 5.5, VEGAN);
        add(foods, 17, "Curd rice", RICE_AND_BREADS, "1 bowl (200 g)", 220, 6.0, 34.0, 6.8, VEGETARIAN);
        add(foods, 18, "Lemon rice", RICE_AND_BREADS, "1 bowl (200 g)", 290, 4.5, 46.0, 9.8, VEGAN);
        add(foods, 19, "Vegetable biryani", RICE_AND_BREADS, "1 plate (200 g)", 290, 6.5, 45.0, 9.5, VEGETARIAN);
        add(foods, 20, "Chicken biryani", RICE_AND_BREADS, "1 plate (250 g)", 400, 20.0, 48.0, 14.0, NON_VEGETARIAN);
        add(foods, 21, "Khichdi", RICE_AND_BREADS, "1 bowl (200 g)", 240, 8.5, 40.0, 5.0, VEGETARIAN);
        add(foods, 22, "Whole wheat bread", RICE_AND_BREADS, "1 slice (32 g)", 80, 4.0, 13.7, 1.1, VEGAN);

        // Dals and curries
        add(foods, 23, "Dal (toor dal)", DALS_AND_CURRIES, "1 katori (150 g)", 160, 8.0, 20.0, 5.0, VEGAN);
        add(foods, 24, "Moong dal", DALS_AND_CURRIES, "1 katori (150 g)", 130, 8.5, 18.0, 2.8, VEGAN);
        add(foods, 25, "Sambar", DALS_AND_CURRIES, "1 katori (150 g)", 110, 5.0, 15.0, 3.4, VEGAN);
        add(foods, 26, "Rasam", DALS_AND_CURRIES, "1 katori (150 g)", 60, 2.0, 8.0, 2.2, VEGAN);
        add(foods, 27, "Rajma curry", DALS_AND_CURRIES, "1 katori (150 g)", 170, 8.0, 22.0, 5.5, VEGAN);
        add(foods, 28, "Chole (chana masala)", DALS_AND_CURRIES, "1 katori (150 g)", 210, 9.0, 27.0, 7.3, VEGAN);
        add(foods, 29, "Paneer butter masala", DALS_AND_CURRIES, "1 katori (150 g)", 330, 11.0, 12.0, 26.0, VEGETARIAN);
        add(foods, 30, "Palak paneer", DALS_AND_CURRIES, "1 katori (150 g)", 250, 11.0, 8.0, 19.5, VEGETARIAN);
        add(foods, 31, "Soya chunks curry", DALS_AND_CURRIES, "1 katori (30 g soya)", 170, 16.0, 14.0, 5.4, VEGAN);

        // Vegetables and salads
        add(foods, 32, "Mixed vegetable curry", VEGETABLES, "1 katori (150 g)", 120, 3.0, 12.0, 6.7, VEGAN);
        add(foods, 33, "Aloo sabzi (potato)", VEGETABLES, "1 katori (150 g)", 150, 2.5, 20.0, 6.7, VEGAN);
        add(foods, 34, "Bhindi fry (okra)", VEGETABLES, "1 katori (100 g)", 110, 2.2, 8.0, 7.7, VEGAN);
        add(foods, 35, "Spinach (cooked)", VEGETABLES, "1 cup (180 g)", 41, 5.3, 6.8, 0.5, VEGAN);
        add(foods, 36, "Vegetable salad", VEGETABLES, "1 bowl (100 g)", 25, 1.0, 5.0, 0.2, VEGAN);
        add(foods, 37, "Carrot (raw)", VEGETABLES, "1 medium (60 g)", 25, 0.6, 5.8, 0.1, VEGAN);
        add(foods, 38, "Cucumber (raw)", VEGETABLES, "1 cup sliced (100 g)", 15, 0.7, 3.6, 0.1, VEGAN);
        add(foods, 39, "Tomato (raw)", VEGETABLES, "1 medium (120 g)", 22, 1.1, 4.7, 0.2, VEGAN);

        // Fruits
        add(foods, 40, "Banana", FRUITS, "1 medium (118 g)", 105, 1.3, 27.0, 0.4, VEGAN);
        add(foods, 41, "Apple", FRUITS, "1 medium (180 g)", 95, 0.5, 25.0, 0.3, VEGAN);
        add(foods, 42, "Mango", FRUITS, "1 cup sliced (165 g)", 99, 1.4, 24.7, 0.6, VEGAN);
        add(foods, 43, "Orange", FRUITS, "1 medium (130 g)", 62, 1.2, 15.4, 0.2, VEGAN);
        add(foods, 44, "Papaya", FRUITS, "1 cup cubed (140 g)", 55, 0.7, 13.7, 0.4, VEGAN);
        add(foods, 45, "Guava", FRUITS, "1 medium (100 g)", 68, 2.6, 14.3, 1.0, VEGAN);
        add(foods, 46, "Grapes", FRUITS, "1 cup (150 g)", 104, 1.1, 27.3, 0.2, VEGAN);
        add(foods, 47, "Watermelon", FRUITS, "1 cup diced (150 g)", 46, 0.9, 11.5, 0.2, VEGAN);
        add(foods, 48, "Pomegranate", FRUITS, "1/2 cup seeds (87 g)", 72, 1.5, 16.3, 1.0, VEGAN);

        // Milk and dairy
        add(foods, 49, "Milk (toned)", DAIRY, "1 glass (200 ml)", 116, 6.2, 9.4, 6.0, VEGETARIAN);
        add(foods, 50, "Curd (dahi)", DAIRY, "1 katori (100 g)", 62, 3.5, 4.7, 3.3, VEGETARIAN);
        add(foods, 51, "Paneer", DAIRY, "100 g", 265, 18.3, 1.2, 20.8, VEGETARIAN);
        add(foods, 52, "Buttermilk (chaas)", DAIRY, "1 glass (200 ml)", 37, 1.8, 2.8, 2.0, VEGETARIAN);
        add(foods, 53, "Sweet lassi", DAIRY, "1 glass (200 ml)", 160, 5.5, 24.0, 4.6, VEGETARIAN);

        // Eggs, meat, fish and soy
        add(foods, 54, "Boiled egg", PROTEIN_FOODS, "1 large (50 g)", 78, 6.3, 0.6, 5.3, NON_VEGETARIAN);
        add(foods, 55, "Omelette", PROTEIN_FOODS, "2 eggs (120 g)", 190, 12.5, 1.5, 15.0, NON_VEGETARIAN);
        add(foods, 56, "Chicken curry", PROTEIN_FOODS, "1 katori (150 g)", 240, 20.0, 6.0, 15.0, NON_VEGETARIAN);
        add(foods, 57, "Grilled chicken breast", PROTEIN_FOODS, "100 g", 165, 31.0, 0.0, 3.6, NON_VEGETARIAN);
        add(foods, 58, "Fish curry", PROTEIN_FOODS, "1 katori (150 g)", 200, 18.0, 5.0, 12.0, NON_VEGETARIAN);
        add(foods, 59, "Mutton curry", PROTEIN_FOODS, "1 katori (150 g)", 300, 22.0, 5.0, 21.0, NON_VEGETARIAN);
        add(foods, 60, "Tofu (firm)", PROTEIN_FOODS, "100 g", 144, 17.3, 2.8, 8.7, VEGAN);

        // Snacks and sides
        add(foods, 61, "Samosa", SNACKS, "1 piece (70 g)", 200, 3.5, 22.0, 11.0, VEGAN);
        add(foods, 62, "Dhokla", SNACKS, "2 pieces (60 g)", 95, 4.0, 14.0, 2.6, VEGETARIAN);
        add(foods, 63, "Roasted chana", SNACKS, "1 handful (30 g)", 110, 6.5, 17.0, 1.5, VEGAN);
        add(foods, 64, "Vegetable sandwich", SNACKS, "1 sandwich (120 g)", 215, 8.5, 30.0, 6.5, VEGETARIAN);
        add(foods, 65, "Marie biscuits", SNACKS, "4 biscuits (28 g)", 125, 2.2, 21.0, 3.6, VEGETARIAN);
        add(foods, 66, "Coconut chutney", SNACKS, "2 tablespoons (30 g)", 70, 0.9, 2.5, 6.4, VEGAN);

        // Beverages
        add(foods, 67, "Tea (milk, sugar)", BEVERAGES, "1 cup (150 ml)", 70, 1.8, 10.0, 2.5, VEGETARIAN);
        add(foods, 68, "Coffee (milk, sugar)", BEVERAGES, "1 cup (150 ml)", 80, 2.3, 11.0, 2.8, VEGETARIAN);
        add(foods, 69, "Coconut water", BEVERAGES, "1 glass (240 ml)", 46, 1.7, 8.9, 0.5, VEGAN);
        add(foods, 70, "Fresh orange juice", BEVERAGES, "1 glass (200 ml)", 90, 1.4, 20.8, 0.4, VEGAN);

        // Nuts, seeds and fats
        add(foods, 71, "Almonds", NUTS_AND_FATS, "10 pieces (12 g)", 70, 2.5, 2.6, 6.0, VEGAN);
        add(foods, 72, "Peanuts (roasted)", NUTS_AND_FATS, "1 handful (30 g)", 175, 7.1, 6.5, 14.9, VEGAN);
        add(foods, 73, "Peanut butter", NUTS_AND_FATS, "1 tablespoon (16 g)", 94, 4.0, 3.1, 8.0, VEGAN);
        add(foods, 74, "Ghee", NUTS_AND_FATS, "1 teaspoon (5 g)", 45, 0.0, 0.0, 5.0, VEGETARIAN);

        return foods;
    }

    private static void add(List<Food> foods, int id, String name, FoodCategory category, String serving,
                            double calories, double protein, double carbohydrates, double fat, DietType diet) {
        foods.add(new Food(id, name, category, serving, calories, protein, carbohydrates, fat, diet));
    }
}
