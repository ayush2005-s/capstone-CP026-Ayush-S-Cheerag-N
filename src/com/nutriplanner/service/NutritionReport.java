package com.nutriplanner.service;

import com.nutriplanner.model.BmiResult;
import com.nutriplanner.model.DailyMealPlan;
import com.nutriplanner.model.DailySummary;
import com.nutriplanner.model.Meal;
import com.nutriplanner.model.MealEntry;
import com.nutriplanner.model.NutritionTargets;
import com.nutriplanner.model.NutritionTotals;
import com.nutriplanner.model.User;
import com.nutriplanner.model.UserProfile;
import com.nutriplanner.util.Fmt;
import com.nutriplanner.util.TextTable;
import com.nutriplanner.util.TextTable.Align;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Builds the text of nutrition reports and the tables shared by several screens
 * (meal details and target-versus-intake comparison).
 *
 * The methods return plain text, so the same report can be printed in the
 * terminal or saved to a .txt file.
 */
public class NutritionReport {

    /** Width of the lines used in reports and screen headers. */
    public static final int WIDTH = Fmt.LINE_WIDTH;

    private static final String DISCLAIMER =
            "These figures are estimates from standard formulas and sample food values."
            + System.lineSeparator()
            + "They are not a medical prescription. Please consult a doctor or a registered"
            + System.lineSeparator()
            + "dietitian before making major changes to your diet.";

    private final NutritionTracker tracker;

    public NutritionReport(NutritionTracker tracker) {
        this.tracker = tracker;
    }

    // ------------------------------------------------------------ full reports

    /** The full daily report: profile, BMI, calorie needs, meals, totals and remaining calories. */
    public String buildDailyReport(User user, DailyMealPlan plan, LocalDateTime generatedAt) {
        UserProfile p = user.getProfile();
        DailySummary summary = tracker.summarize(p, plan);
        NutritionTargets t = summary.getTargets();
        BmiResult bmi = BMICalculator.evaluate(p.getWeightKg(), p.getHeightCm());
        StringBuilder sb = new StringBuilder();

        title(sb, "NUTRIPLANNER - DAILY NUTRITION REPORT");
        field(sb, "Report for", Fmt.date(plan.getDate()));
        field(sb, "Generated on", Fmt.dateTime(generatedAt));

        section(sb, "1. USER PROFILE");
        field(sb, "Name", p.getName() + " (username: " + user.getUsername() + ")");
        field(sb, "Age and gender", p.getAge() + " years, " + p.getGender().getLabel());
        field(sb, "Height and weight", Fmt.oneDecimal(p.getHeightCm()) + " cm, " + Fmt.oneDecimal(p.getWeightKg()) + " kg");
        field(sb, "Activity level", p.getActivityLevel().getLabel() + " (" + p.getActivityLevel().getDescription() + ")");
        field(sb, "Dietary preference", p.getDietType().getLabel());
        field(sb, "Health goal", p.getHealthGoal().getLabel());

        section(sb, "2. BMI RESULT");
        field(sb, "BMI", Fmt.oneDecimal(bmi.getBmi()) + " (" + bmi.getCategory().getLabel() + ")");
        field(sb, "Healthy weight range", Fmt.oneDecimal(bmi.getHealthyMinWeightKg()) + " - "
                + Fmt.oneDecimal(bmi.getHealthyMaxWeightKg()) + " kg for your height");

        section(sb, "3. DAILY CALORIE REQUIREMENTS");
        field(sb, "BMR (Mifflin-St Jeor)", Fmt.kcal(t.getBmr()) + " kcal");
        field(sb, "TDEE (BMR x " + p.getActivityLevel().getMultiplier() + ")", Fmt.kcal(t.getTdee()) + " kcal");
        field(sb, "Goal adjustment", Fmt.signedKcal(t.getGoal().getCalorieAdjustment()) + " kcal ("
                + t.getGoal().getLabel() + ")");
        field(sb, "Daily calorie target", Fmt.kcal(t.getCalorieTarget()) + " kcal"
                + (!t.isMinimumApplied() ? ""
                        : t.getCalorieTarget() >= t.getMinimumCalories() ? " (raised to the safe minimum)"
                        : " (set to TDEE, as TDEE is below the safe minimum)"));
        field(sb, "Macronutrient targets", "Protein " + t.getProteinGrams() + " g, Carbohydrates "
                + t.getCarbohydrateGrams() + " g, Fat " + t.getFatGrams() + " g");

        section(sb, "4. MEAL DETAILS");
        sb.append(renderMealDetails(plan));

        section(sb, "5. TOTAL NUTRITIONAL INTAKE");
        sb.append(renderComparison(summary));

        section(sb, "6. REMAINING CALORIE ALLOWANCE");
        double remaining = summary.getRemainingCalories();
        if (remaining >= 0) {
            line(sb, "Remaining today: " + Fmt.kcal(remaining) + " kcal of your "
                    + Fmt.kcal(t.getCalorieTarget()) + " kcal target.");
        } else {
            line(sb, "Over target by: " + Fmt.kcal(-remaining) + " kcal (target "
                    + Fmt.kcal(t.getCalorieTarget()) + " kcal).");
        }
        for (String advice : tracker.buildAdvice(summary, p.getDietType())) {
            wrapped(sb, "- ", advice);
        }

        sb.append(System.lineSeparator());
        rule(sb, '-');
        line(sb, DISCLAIMER);
        rule(sb, '=');
        return sb.toString();
    }

    /** A summary of the last days: calories and macros per day compared with the target. */
    public String buildWeeklySummary(User user, List<DailyMealPlan> plans, LocalDateTime generatedAt) {
        UserProfile p = user.getProfile();
        NutritionTargets t = NutritionCalculator.calculateTargets(p);
        StringBuilder sb = new StringBuilder();
        LocalDate first = plans.get(0).getDate();
        LocalDate last = plans.get(plans.size() - 1).getDate();

        title(sb, "NUTRIPLANNER - " + plans.size() + "-DAY NUTRITION SUMMARY");
        field(sb, "Name", p.getName() + " (username: " + user.getUsername() + ")");
        field(sb, "Period", Fmt.shortDate(first) + " to " + Fmt.shortDate(last));
        field(sb, "Daily calorie target", Fmt.kcal(t.getCalorieTarget()) + " kcal (" + t.getGoal().getLabel() + ")");
        field(sb, "Generated on", Fmt.dateTime(generatedAt));
        sb.append(System.lineSeparator());

        TextTable table = new TextTable().indent(2)
                .addColumn("Date", 16, Align.LEFT)
                .addColumn("kcal", 6, Align.RIGHT)
                .addColumn("Target", 6, Align.RIGHT)
                .addColumn("Diff", 7, Align.RIGHT)
                .addColumn("Protein", 7, Align.RIGHT)
                .addColumn("Carbs", 6, Align.RIGHT)
                .addColumn("Fat", 5, Align.RIGHT)
                .addColumn("Foods", 5, Align.RIGHT);
        NutritionTotals loggedTotal = NutritionTotals.ZERO;
        int loggedDays = 0;
        for (DailyMealPlan plan : plans) {
            boolean logged = !plan.isEmpty();
            table.addRow(Fmt.date(plan.getDate()),
                    logged ? Fmt.kcal(plan.getCalories()) : "-",
                    Fmt.kcal(t.getCalorieTarget()),
                    logged ? Fmt.signedKcal(plan.getCalories() - t.getCalorieTarget()) : "-",
                    logged ? Fmt.oneDecimal(plan.getProtein()) : "-",
                    logged ? Fmt.oneDecimal(plan.getCarbohydrates()) : "-",
                    logged ? Fmt.oneDecimal(plan.getFat()) : "-",
                    String.valueOf(plan.getEntryCount()));
            if (logged) {
                loggedTotal = loggedTotal.plus(plan);
                loggedDays++;
            }
        }
        sb.append(table.render());
        sb.append(System.lineSeparator());
        line(sb, "Days with food logged: " + loggedDays + " of " + plans.size());
        if (loggedDays > 0) {
            wrapped(sb, "", "Average on logged days: " + Fmt.kcal(loggedTotal.getCalories() / loggedDays) + " kcal, protein "
                    + Fmt.oneDecimal(loggedTotal.getProtein() / loggedDays) + " g, carbohydrates "
                    + Fmt.oneDecimal(loggedTotal.getCarbohydrates() / loggedDays) + " g, fat "
                    + Fmt.oneDecimal(loggedTotal.getFat() / loggedDays) + " g");
            double difference = loggedTotal.getCalories() / loggedDays - t.getCalorieTarget();
            wrapped(sb, "", "On average you ate " + Fmt.kcal(Math.abs(difference)) + " kcal "
                    + (difference >= 0 ? "above" : "below") + " your target on the days you logged.");
        } else {
            line(sb, "No meals were logged in this period. Use the Meal Planner to add your meals.");
        }
        wrapped(sb, "", "Days without any food logged are shown with '-'. Targets use your current profile.");
        sb.append(System.lineSeparator());
        rule(sb, '-');
        line(sb, DISCLAIMER);
        rule(sb, '=');
        return sb.toString();
    }

    /** A safe file name for a saved report, for example "priya_2026-11-17_daily.txt". */
    public static String reportFileName(String username, LocalDate date, String kind) {
        return username + "_" + date + "_" + kind + ".txt";
    }

    // ------------------------------------------------------- shared sections

    /** Every meal of the day with its foods and subtotal, then the day total. */
    public String renderMealDetails(DailyMealPlan plan) {
        StringBuilder sb = new StringBuilder();
        for (Meal meal : plan.getMeals()) {
            line(sb, meal.getType().getLabel().toUpperCase(java.util.Locale.ROOT) + "  (" + meal.size() + " item"
                    + (meal.size() == 1 ? "" : "s") + ", " + Fmt.kcal(meal.getCalories()) + " kcal)");
            if (meal.isEmpty()) {
                line(sb, "  No foods added yet.");
            } else {
                sb.append(mealTable(meal, true).render());
            }
            sb.append(System.lineSeparator());
        }
        wrapped(sb, "", "DAY TOTAL: " + Fmt.kcal(plan.getCalories()) + " kcal, protein " + Fmt.oneDecimal(plan.getProtein())
                + " g, carbohydrates " + Fmt.oneDecimal(plan.getCarbohydrates()) + " g, fat "
                + Fmt.oneDecimal(plan.getFat()) + " g");
        line(sb, "Servings are multiples of each food's serving size. Nutrients are in grams.");
        return sb.toString();
    }

    /** Lists the foods of one meal with item numbers, used when choosing an item to change. */
    public String renderMeal(Meal meal) {
        if (meal.isEmpty()) {
            return "  " + meal.getType().getLabel() + " has no foods yet." + System.lineSeparator();
        }
        return mealTable(meal, false).render();
    }

    /** Target, consumed, remaining and progress for calories and each macronutrient. */
    public String renderComparison(DailySummary summary) {
        NutritionTargets t = summary.getTargets();
        NutritionTotals c = summary.getConsumed();
        TextTable table = new TextTable().indent(2)
                .addColumn("Nutrient", 15, Align.LEFT)
                .addColumn("Target", 7, Align.RIGHT)
                .addColumn("Eaten", 7, Align.RIGHT)
                .addColumn("Remaining", 9, Align.RIGHT)
                .addColumn("Progress", 27, Align.LEFT);
        table.addRow("Calories (kcal)", Fmt.kcal(t.getCalories()), Fmt.kcal(c.getCalories()),
                Fmt.kcal(summary.getRemainingCalories()), progress(c.getCalories(), t.getCalories()));
        table.addRow("Protein (g)", Fmt.kcal(t.getProtein()), Fmt.oneDecimal(c.getProtein()),
                Fmt.oneDecimal(summary.getRemainingProtein()), progress(c.getProtein(), t.getProtein()));
        table.addRow("Carbs (g)", Fmt.kcal(t.getCarbohydrates()), Fmt.oneDecimal(c.getCarbohydrates()),
                Fmt.oneDecimal(summary.getRemainingCarbohydrates()), progress(c.getCarbohydrates(), t.getCarbohydrates()));
        table.addRow("Fat (g)", Fmt.kcal(t.getFat()), Fmt.oneDecimal(c.getFat()),
                Fmt.oneDecimal(summary.getRemainingFat()), progress(c.getFat(), t.getFat()));
        return table.render() + "  A negative remaining value means the target was exceeded." + System.lineSeparator();
    }

    // ------------------------------------------------------------- helpers

    /** A numbered table of a meal's foods, optionally followed by the meal total. */
    private static TextTable mealTable(Meal meal, boolean withTotal) {
        TextTable table = new TextTable().indent(2)
                .addColumn("No.", 3, Align.RIGHT)
                .addColumn("Food", 24, Align.LEFT)
                .addColumn("Servings", 8, Align.RIGHT)
                .addColumn("kcal", 6, Align.RIGHT)
                .addColumn("Protein", 7, Align.RIGHT)
                .addColumn("Carbs", 6, Align.RIGHT)
                .addColumn("Fat", 5, Align.RIGHT);
        int number = 1;
        for (MealEntry entry : meal.getEntries()) {
            table.addRow(String.valueOf(number++), entry.getFoodName(), Fmt.number(entry.getServings()),
                    Fmt.kcal(entry.getCalories()), Fmt.oneDecimal(entry.getProtein()),
                    Fmt.oneDecimal(entry.getCarbohydrates()), Fmt.oneDecimal(entry.getFat()));
        }
        if (withTotal) {
            table.addSeparator();
            table.addRow("", meal.getType().getLabel() + " total", "", Fmt.kcal(meal.getCalories()),
                    Fmt.oneDecimal(meal.getProtein()), Fmt.oneDecimal(meal.getCarbohydrates()),
                    Fmt.oneDecimal(meal.getFat()));
        }
        return table;
    }

    private static String progress(double eaten, double target) {
        return Fmt.progressBar(eaten, target, 20) + " " + Fmt.percent(eaten, target) + "%";
    }

    private static void title(StringBuilder sb, String text) {
        rule(sb, '=');
        line(sb, Fmt.center(text, WIDTH));
        rule(sb, '=');
    }

    private static void section(StringBuilder sb, String text) {
        sb.append(System.lineSeparator());
        line(sb, text);
        rule(sb, '-');
    }

    private static void field(StringBuilder sb, String label, String value) {
        line(sb, Fmt.padRight(label, 22) + ": " + value);
    }

    private static void rule(StringBuilder sb, char c) {
        line(sb, Fmt.repeat(c, WIDTH));
    }

    private static void wrapped(StringBuilder sb, String prefix, String text) {
        for (String part : Fmt.wrap(prefix, text, WIDTH)) {
            line(sb, part);
        }
    }

    private static void line(StringBuilder sb, String text) {
        sb.append(text).append(System.lineSeparator());
    }
}
