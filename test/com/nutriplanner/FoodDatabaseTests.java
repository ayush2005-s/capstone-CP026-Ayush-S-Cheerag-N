package com.nutriplanner;

import com.nutriplanner.exception.ValidationException;
import com.nutriplanner.model.CustomFood;
import com.nutriplanner.model.DietType;
import com.nutriplanner.model.Food;
import com.nutriplanner.model.FoodCategory;
import com.nutriplanner.service.FoodDatabase;
import com.nutriplanner.storage.DataStorage;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.nutriplanner.TestSupport.assertEquals;
import static com.nutriplanner.TestSupport.assertThrows;
import static com.nutriplanner.TestSupport.check;

/** Tests for the food database: contents, search, diet filter and custom foods. */
public final class FoodDatabaseTests {

    private FoodDatabaseTests() {
    }

    public static void run(TestSupport t) throws Exception {
        Path folder = TestSupport.tempFolder();
        try {
            FoodDatabase db = new FoodDatabase(new DataStorage(folder));

            t.test("Database contains the required Indian foods", () -> {
                String[] required = {"idli", "dosa", "chapati", "rice", "dal", "paneer", "curd", "milk",
                    "upma", "poha", "roti", "sambar"};
                for (String name : required) {
                    check(!db.search(name).isEmpty(), "missing food: " + name);
                }
                check(db.getByCategory(FoodCategory.FRUITS).size() >= 5, "at least 5 fruits");
                check(db.getByCategory(FoodCategory.VEGETABLES).size() >= 5, "at least 5 vegetables");
                check(db.size() >= 70, "at least 70 foods");
            });

            t.test("Food IDs and names are unique and values are sensible", () -> {
                Set<Integer> ids = new HashSet<>();
                Set<String> names = new HashSet<>();
                for (Food food : db.getAllFoods()) {
                    check(ids.add(food.getId()), "duplicate ID " + food.getId());
                    check(names.add(food.getName().toLowerCase(Locale.ROOT)), "duplicate name " + food.getName());
                    double fromMacros = food.getProtein() * 4 + food.getCarbohydrates() * 4 + food.getFat() * 9;
                    check(Math.abs(fromMacros - food.getCalories()) <= Math.max(25, food.getCalories() * 0.3),
                            food.getName() + ": calories do not match its macros");
                }
            });

            t.test("Search ignores case and lists names that start with the text first", () -> {
                List<Food> results = db.search("DAL");
                check(results.size() >= 2, "several dals");
                assertEquals("Dal (toor dal)", results.get(0).getName(), "name starting with 'dal' comes first");
                for (Food food : results) {
                    check(food.getName().toLowerCase(Locale.ROOT).contains("dal"), "unrelated result " + food);
                }
            });

            t.test("Search matches the start of words only", () -> {
                List<Food> tea = db.search("tea");
                assertEquals(1, tea.size(), "only tea, not 'Steamed rice'");
                assertEquals("Chapati (with oil)", db.search("chap").get(0).getName(), "word prefix");
                check(!db.search("chana").isEmpty() && db.search("chana").size() >= 2, "chole (chana masala) and roasted chana");
            });

            t.test("Search falls back to categories, so 'fruit' lists fruits", () -> {
                List<Food> results = db.search("fruit");
                check(results.size() >= 5, "fruits found");
                for (Food food : results) {
                    assertEquals(FoodCategory.FRUITS, food.getCategory(), food.getName());
                }
            });

            t.test("Search needs at least two letters and handles no matches", () -> {
                assertThrows(ValidationException.class, () -> db.search("a"), "one letter");
                assertThrows(ValidationException.class, () -> db.search("   "), "blank");
                check(db.search("pizza").isEmpty(), "no pizza in the database");
            });

            t.test("Diet filter: vegans and vegetarians only see suitable foods", () -> {
                for (Food food : db.getSuitableFoods(DietType.VEGAN)) {
                    assertEquals(DietType.VEGAN, food.getDietType(), food.getName());
                }
                for (Food food : db.getSuitableFoods(DietType.VEGETARIAN)) {
                    check(food.getDietType() != DietType.NON_VEGETARIAN, "non-veg food for vegetarian: " + food);
                }
                assertEquals(db.size(), db.getSuitableFoods(DietType.NON_VEGETARIAN).size(), "non-veg sees all");
                check(DietType.VEGETARIAN.allows(DietType.VEGAN), "vegetarians can eat vegan food");
                check(!DietType.VEGAN.allows(DietType.VEGETARIAN), "vegans do not eat dairy");
            });

            t.test("Custom food gets ID 1001, is saved and is found after a restart", () -> {
                CustomFood food = db.addCustomFood("Ragi dosa", FoodCategory.BREAKFAST, "1 piece (70 g)",
                        110, 3, 20, 2, DietType.VEGAN, "priya");
                assertEquals(1001, food.getId(), "first custom ID");
                check(!food.isBuiltIn(), "custom food is not built in");
                FoodDatabase reloaded = new FoodDatabase(new DataStorage(folder));
                Food again = reloaded.findById(1001).orElseThrow(() -> new AssertionError("not reloaded"));
                assertEquals("Ragi dosa", again.getName(), "name");
                assertEquals("Added by user 'priya'", again.getSourceLabel(), "source label (polymorphism)");
                CustomFood second = reloaded.addCustomFood("Sprouts salad, homemade", FoodCategory.SNACKS,
                        "1 bowl (100 g)", 100, 7, 15, 1, DietType.VEGAN, "rahul");
                assertEquals(1002, second.getId(), "next ID");
                assertEquals("Sprouts salad, homemade",
                        new FoodDatabase(new DataStorage(folder)).findById(1002).get().getName(), "comma kept");
            });

            t.test("Invalid or duplicate custom foods are rejected", () -> {
                assertThrows(ValidationException.class, () -> db.addCustomFood("idli", FoodCategory.BREAKFAST,
                        "1 piece", 50, 2, 10, 0, DietType.VEGAN, "x"), "duplicate name");
                assertThrows(ValidationException.class, () -> db.addCustomFood("Test food", FoodCategory.OTHER,
                        "1 bowl", -10, 2, 10, 0, DietType.VEGAN, "x"), "negative calories");
                assertThrows(ValidationException.class, () -> db.addCustomFood("Test food", FoodCategory.OTHER,
                        "", 10, 2, 10, 0, DietType.VEGAN, "x"), "empty serving");
                assertThrows(ValidationException.class, () -> db.addCustomFood("X", FoodCategory.OTHER,
                        "1 bowl", 10, 2, 10, 0, DietType.VEGAN, "x"), "name too short");
            });
        } finally {
            TestSupport.deleteFolder(folder);
        }
    }
}
