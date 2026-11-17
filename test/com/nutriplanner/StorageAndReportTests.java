package com.nutriplanner;

import com.nutriplanner.model.ActivityLevel;
import com.nutriplanner.model.DailyMealPlan;
import com.nutriplanner.model.DailySummary;
import com.nutriplanner.model.DietType;
import com.nutriplanner.model.Gender;
import com.nutriplanner.model.HealthGoal;
import com.nutriplanner.model.MealType;
import com.nutriplanner.model.User;
import com.nutriplanner.model.UserProfile;
import com.nutriplanner.service.FoodDatabase;
import com.nutriplanner.service.MealPlanner;
import com.nutriplanner.service.NutritionReport;
import com.nutriplanner.service.NutritionTracker;
import com.nutriplanner.storage.CsvUtil;
import com.nutriplanner.storage.DataStorage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.nutriplanner.TestSupport.assertContains;
import static com.nutriplanner.TestSupport.assertEquals;
import static com.nutriplanner.TestSupport.assertNear;
import static com.nutriplanner.TestSupport.assertThrows;
import static com.nutriplanner.TestSupport.check;

/** Tests for CSV files, damaged or missing data files, tracking and reports. */
public final class StorageAndReportTests {

    private StorageAndReportTests() {
    }

    public static void run(TestSupport t) throws Exception {
        t.test("CSV lines keep commas and quotes inside values", () -> {
            List<String> values = Arrays.asList("plain", "with, comma", "say \"hi\"", "");
            String line = CsvUtil.toLine(values);
            assertEquals("plain,\"with, comma\",\"say \"\"hi\"\"\",", line, "written form");
            assertEquals(values, CsvUtil.parseLine(line), "read back");
            assertThrows(IllegalArgumentException.class, () -> CsvUtil.parseLine("\"not closed"), "unclosed quote");
        });

        t.test("Missing data files mean an empty start, not an error", () -> {
            Path folder = TestSupport.tempFolder().resolve("new-folder");
            try {
                DataStorage storage = new DataStorage(folder);
                check(Files.isDirectory(folder), "folder created");
                check(storage.loadUsers().isEmpty(), "no users");
                check(storage.loadMealPlans().isEmpty(), "no meals");
                check(storage.loadCustomFoods().isEmpty(), "no custom foods");
                check(storage.getWarnings().isEmpty(), "no warnings");
            } finally {
                TestSupport.deleteFolder(folder.getParent());
            }
        });

        t.test("Damaged lines are skipped with a warning and good lines still load", () -> {
            Path folder = TestSupport.tempFolder();
            try {
                String header = "username,date,meal,food_id,food_name,serving_size,calories,protein_g,"
                        + "carbohydrates_g,fat_g,servings";
                String good = "priya,2026-11-17,BREAKFAST,1,Idli,1 piece (40 g),58,2,12,0.2,2";
                String badNumber = "priya,2026-11-17,LUNCH,1,Idli,1 piece (40 g),lots,2,12,0.2,2";
                String fewValues = "priya,2026-11-17,LUNCH";
                String badMeal = "priya,2026-11-17,BRUNCH,1,Idli,1 piece (40 g),58,2,12,0.2,2";
                String unclosed = "priya,\"2026-11-17,LUNCH,1,Idli,1 piece,58,2,12,0.2,2";
                Files.write(folder.resolve(DataStorage.MEAL_PLANS_FILE), Arrays.asList(header, good, badNumber,
                        fewValues, badMeal, unclosed), StandardCharsets.UTF_8);
                DataStorage storage = new DataStorage(folder);
                List<DailyMealPlan> plans = storage.loadMealPlans();
                assertEquals(1, plans.size(), "one plan");
                assertNear(116, plans.get(0).getCalories(), 1e-9, "only the good line counted");
                assertEquals(4, storage.getWarnings().size(), "four warnings");
                String allWarnings = String.join(" | ", storage.getWarnings());
                for (String line : new String[] {"line 3 ", "line 4 ", "line 5 ", "line 6 "}) {
                    assertContains(allWarnings, line, "each damaged line is named");
                }
            } finally {
                TestSupport.deleteFolder(folder);
            }
        });

        Path folder = TestSupport.tempFolder();
        try {
            DataStorage storage = new DataStorage(folder);
            FoodDatabase foods = new FoodDatabase(storage);
            MealPlanner planner = new MealPlanner(storage);
            NutritionTracker tracker = new NutritionTracker();
            NutritionReport report = new NutritionReport(tracker);
            UserProfile profile = new UserProfile("Priya Sharma", 21, Gender.FEMALE, 160, 55,
                    ActivityLevel.LIGHTLY_ACTIVE, DietType.VEGETARIAN, HealthGoal.WEIGHT_LOSS);
            User user = new User("priya", "unused", profile, LocalDate.of(2026, 11, 1));
            LocalDate day = LocalDate.of(2026, 11, 17);

            t.test("Daily nutrition tracking compares intake with targets", () -> {
                DailySummary empty = tracker.summarize(profile, planner.getPlan("priya", day));
                assertEquals(1266, empty.getTargets().getCalorieTarget(), "target");
                assertNear(1266, empty.getRemainingCalories(), 1e-9, "nothing eaten yet");
                assertContains(tracker.buildAdvice(empty, DietType.VEGETARIAN).get(0), "No food has been logged",
                        "advice for an empty day");

                planner.addFood("priya", day, MealType.BREAKFAST, foods.findById(5).get(), 1);     // poha 180
                planner.addFood("priya", day, MealType.LUNCH, foods.findById(12).get(), 1);        // rice 195
                planner.addFood("priya", day, MealType.LUNCH, foods.findById(23).get(), 1);        // dal 160
                planner.addFood("priya", day, MealType.EVENING_SNACKS, foods.findById(67).get(), 1); // tea 70
                planner.addFood("priya", day, MealType.DINNER, foods.findById(14).get(), 2);       // chapati 240
                DailySummary summary = tracker.summarize(profile, planner.getPlan("priya", day));
                assertNear(845, summary.getConsumed().getCalories(), 1e-9, "calories consumed");
                assertNear(421, summary.getRemainingCalories(), 1e-9, "remaining calories");
                assertNear(240, summary.getCaloriesByMeal().get(MealType.DINNER), 1e-9, "dinner calories");
                assertContains(tracker.buildAdvice(summary, DietType.VEGETARIAN).get(0), "421 kcal",
                        "advice shows remaining calories");
            });

            t.test("Over-target days are reported", () -> {
                LocalDate feast = day.plusDays(1);
                planner.addFood("priya", feast, MealType.DINNER, foods.findById(29).get(), 5); // 1650 kcal
                DailySummary summary = tracker.summarize(profile, planner.getPlan("priya", feast));
                assertNear(-384, summary.getRemainingCalories(), 1e-9, "negative remaining");
                List<String> advice = tracker.buildAdvice(summary, DietType.VEGETARIAN);
                assertContains(advice.get(0), "384 kcal over", "over-target message");
            });

            t.test("Report generation includes every required section", () -> {
                String text = report.buildDailyReport(user, planner.getPlan("priya", day),
                        LocalDateTime.of(2026, 11, 17, 20, 30));
                String[] required = {"1. USER PROFILE", "2. BMI RESULT", "3. DAILY CALORIE REQUIREMENTS",
                    "4. MEAL DETAILS", "5. TOTAL NUTRITIONAL INTAKE", "6. REMAINING CALORIE ALLOWANCE",
                    "Priya Sharma", "21.5 (Normal weight)", "1,266 kcal", "BREAKFAST", "EVENING SNACKS",
                    "Poha", "Remaining today: 421 kcal", "not a medical prescription"};
                for (String part : required) {
                    assertContains(text, part, "daily report");
                }
                for (String line : text.split("\\R")) {
                    check(line.length() <= 78, "report line too wide: " + line);
                }
            });

            t.test("7-day summary and saving a report to a text file", () -> {
                List<DailyMealPlan> week = planner.getPlansBetween("priya", day.minusDays(5), day.plusDays(1));
                String text = report.buildWeeklySummary(user, week, LocalDateTime.of(2026, 11, 18, 9, 0));
                assertContains(text, "Days with food logged: 2 of 7", "logged days");
                assertContains(text, "Tue, 17 Nov 2026", "a day row");
                Path saved = storage.saveReport(NutritionReport.reportFileName("priya", day, "weekly"), text);
                check(Files.exists(saved), "report file exists");
                assertEquals("priya_2026-11-17_weekly.txt", saved.getFileName().toString(), "file name");
                assertContains(new String(Files.readAllBytes(saved), StandardCharsets.UTF_8),
                        "Days with food logged", "file content");
            });
        } finally {
            TestSupport.deleteFolder(folder);
        }
    }
}
