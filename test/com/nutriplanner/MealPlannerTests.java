package com.nutriplanner;

import com.nutriplanner.exception.ValidationException;
import com.nutriplanner.model.DailyMealPlan;
import com.nutriplanner.model.Food;
import com.nutriplanner.model.Meal;
import com.nutriplanner.model.MealType;
import com.nutriplanner.service.FoodDatabase;
import com.nutriplanner.service.MealPlanner;
import com.nutriplanner.storage.DataStorage;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static com.nutriplanner.TestSupport.assertEquals;
import static com.nutriplanner.TestSupport.assertNear;
import static com.nutriplanner.TestSupport.assertThrows;
import static com.nutriplanner.TestSupport.check;

/** Tests for meal creation, meal-wise totals, changes and saving. */
public final class MealPlannerTests {

    private MealPlannerTests() {
    }

    public static void run(TestSupport t) throws Exception {
        Path folder = TestSupport.tempFolder();
        try {
            DataStorage storage = new DataStorage(folder);
            FoodDatabase foods = new FoodDatabase(storage);
            MealPlanner planner = new MealPlanner(storage);
            LocalDate day = LocalDate.of(2026, 11, 17);
            Food idli = foods.findById(1).get();
            Food sambar = foods.findById(25).get();
            Food chapati = foods.findById(14).get();
            Food dal = foods.findById(23).get();

            t.test("Meal creation and meal nutritional calculations", () -> {
                planner.addFood("priya", day, MealType.BREAKFAST, idli, 2);
                planner.addFood("priya", day, MealType.BREAKFAST, sambar, 1);
                Meal breakfast = planner.getPlan("priya", day).getMeal(MealType.BREAKFAST);
                assertEquals(2, breakfast.size(), "two foods");
                assertNear(226, breakfast.getCalories(), 1e-9, "2 x 58 + 110 kcal");
                assertNear(9.0, breakfast.getProtein(), 1e-9, "2 x 2.0 + 5.0 g protein");
                assertNear(39.0, breakfast.getCarbohydrates(), 1e-9, "2 x 12 + 15 g carbohydrates");
                assertNear(3.8, breakfast.getFat(), 1e-9, "2 x 0.2 + 3.4 g fat");
            });

            t.test("Daily total adds up all four meals", () -> {
                planner.addFood("priya", day, MealType.LUNCH, chapati, 2);
                planner.addFood("priya", day, MealType.LUNCH, dal, 1.5);
                DailyMealPlan plan = planner.getPlan("priya", day);
                // breakfast 226 + lunch (240 + 240) = 706
                assertNear(706, plan.getCalories(), 1e-9, "day calories");
                assertEquals(4, plan.getEntryCount(), "four entries");
                check(plan.getMeal(MealType.DINNER).isEmpty(), "dinner is empty");
            });

            t.test("Servings can be changed and foods removed", () -> {
                planner.updateServings("priya", day, MealType.BREAKFAST, 0, 3);
                assertNear(284, planner.getPlan("priya", day).getMeal(MealType.BREAKFAST).getCalories(), 1e-9,
                        "3 x 58 + 110");
                planner.removeFood("priya", day, MealType.BREAKFAST, 1);
                assertNear(174, planner.getPlan("priya", day).getMeal(MealType.BREAKFAST).getCalories(), 1e-9,
                        "sambar removed");
                assertThrows(ValidationException.class,
                        () -> planner.removeFood("priya", day, MealType.BREAKFAST, 5), "no such item");
            });

            t.test("Invalid food quantities are rejected", () -> {
                assertThrows(ValidationException.class,
                        () -> planner.addFood("priya", day, MealType.DINNER, idli, 0), "zero servings");
                assertThrows(ValidationException.class,
                        () -> planner.addFood("priya", day, MealType.DINNER, idli, -2), "negative servings");
                assertThrows(ValidationException.class,
                        () -> planner.addFood("priya", day, MealType.DINNER, idli, 25), "too many servings");
                assertThrows(ValidationException.class,
                        () -> planner.updateServings("priya", day, MealType.LUNCH, 0, 0), "update to zero");
                check(planner.getPlan("priya", day).getMeal(MealType.DINNER).isEmpty(), "nothing was added");
            });

            t.test("Meals are kept separately for each user and date", () -> {
                planner.addFood("rahul", day, MealType.DINNER, dal, 1);
                planner.addFood("priya", day.plusDays(1), MealType.DINNER, idli, 1);
                assertEquals(1, planner.getPlan("rahul", day).getEntryCount(), "rahul's day");
                assertEquals(3, planner.getPlan("priya", day).getEntryCount(), "priya's day unchanged");
                assertEquals(1, planner.getPlan("priya", day.plusDays(1)).getEntryCount(), "priya's next day");
            });

            t.test("Data persistence: meals are reloaded after a restart", () -> {
                MealPlanner reloaded = new MealPlanner(new DataStorage(folder));
                DailyMealPlan plan = reloaded.getPlan("priya", day);
                assertNear(planner.getPlan("priya", day).getCalories(), plan.getCalories(), 1e-9, "same calories");
                assertEquals("Idli", plan.getMeal(MealType.BREAKFAST).getEntry(0).getFoodName(), "food name");
                assertNear(3, plan.getMeal(MealType.BREAKFAST).getEntry(0).getServings(), 1e-9, "servings");
                assertNear(1.5, plan.getMeal(MealType.LUNCH).getEntry(1).getServings(), 1e-9, "half servings");
            });

            t.test("Clearing a meal and listing a week of plans", () -> {
                int removed = planner.clearMeal("priya", day, MealType.LUNCH);
                assertEquals(2, removed, "two lunch foods removed");
                List<DailyMealPlan> week = planner.getPlansBetween("priya", day.minusDays(6), day);
                assertEquals(7, week.size(), "seven days");
                assertEquals(day, week.get(6).getDate(), "last day");
                check(week.get(0).isEmpty(), "older days are empty");
            });
        } finally {
            TestSupport.deleteFolder(folder);
        }
    }
}
