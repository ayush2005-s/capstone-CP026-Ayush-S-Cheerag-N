package com.nutriplanner.ui;

import com.nutriplanner.model.BmiCategory;
import com.nutriplanner.model.HealthGoal;
import com.nutriplanner.model.NutritionTargets;
import com.nutriplanner.model.User;
import com.nutriplanner.model.UserProfile;
import com.nutriplanner.service.BMICalculator;
import com.nutriplanner.service.NutritionCalculator;
import com.nutriplanner.util.Fmt;

/**
 * Shows how the daily calorie requirement is worked out, step by step,
 * using the logged-in user's profile.
 */
public class CalorieScreen {

    private final ConsoleView view;
    private final ConsoleInput input;
    private final User user;

    public CalorieScreen(AppContext context, User user) {
        this.view = context.getView();
        this.input = context.getInput();
        this.user = user;
    }

    public void show() {
        UserProfile p = user.getProfile();
        NutritionTargets t = NutritionCalculator.calculateTargets(p);
        HealthGoal goal = p.getHealthGoal();

        view.blankLine();
        view.header("DAILY CALORIE REQUIREMENTS");
        view.println("Based on: " + p.getGender().getLabel() + ", " + p.getAge() + " years, "
                + Fmt.oneDecimal(p.getHeightCm()) + " cm, " + Fmt.oneDecimal(p.getWeightKg()) + " kg, "
                + p.getActivityLevel().getLabel().toLowerCase(java.util.Locale.ROOT));

        view.section("STEP 1: BASAL METABOLIC RATE (BMR)");
        view.println("Energy your body uses at complete rest (Mifflin-St Jeor equation):");
        view.println("  BMR = 10 x weight + 6.25 x height - 5 x age " + signed(p.getGender().getBmrConstant()));
        view.println("      = 10 x " + Fmt.number(p.getWeightKg()) + " + 6.25 x " + Fmt.number(p.getHeightCm())
                + " - 5 x " + p.getAge() + " " + signed(p.getGender().getBmrConstant()));
        view.println("      = " + Fmt.kcal(t.getBmr()) + " kcal per day");

        view.section("STEP 2: TOTAL DAILY ENERGY EXPENDITURE (TDEE)");
        view.println("Calories you burn on a typical day, including activity:");
        view.println("  TDEE = BMR x " + p.getActivityLevel().getMultiplier() + " ("
                + p.getActivityLevel().getLabel() + ")");
        view.println("       = " + Fmt.kcal(t.getBmr()) + " x " + p.getActivityLevel().getMultiplier()
                + " = " + Fmt.kcal(t.getTdee()) + " kcal per day");

        view.section("STEP 3: ADJUST FOR YOUR GOAL (" + goal.getLabel().toUpperCase(java.util.Locale.ROOT) + ")");
        view.println("  " + goal.getLabel() + ": " + goal.getDescription() + " ("
                + Fmt.signedKcal(goal.getCalorieAdjustment()) + " kcal).");
        view.println("  " + Fmt.kcal(t.getTdee()) + " " + signed(goal.getCalorieAdjustment()) + " = "
                + Fmt.kcal(Math.round(t.getTdee()) + goal.getCalorieAdjustment()) + " kcal");
        if (t.isMinimumApplied() && t.getCalorieTarget() >= t.getMinimumCalories()) {
            view.info("This is below the safe minimum of " + Fmt.kcal(t.getMinimumCalories())
                    + " kcal a day, so your target has been raised to " + Fmt.kcal(t.getCalorieTarget()) + " kcal.");
        } else if (t.isMinimumApplied()) {
            view.info("This is below the safe minimum of " + Fmt.kcal(t.getMinimumCalories())
                    + " kcal a day. Because you burn only about " + Fmt.kcal(t.getTdee())
                    + " kcal, your target is set to that amount, which keeps your weight steady."
                    + " Please talk to a doctor or dietitian about safe weight loss.");
        }

        view.section("YOUR RESULT");
        view.field("BMR", Fmt.kcal(t.getBmr()) + " kcal");
        view.field("TDEE", Fmt.kcal(t.getTdee()) + " kcal");
        view.field("Daily calorie target", Fmt.kcal(t.getCalorieTarget()) + " kcal");
        view.blankLine();
        view.println("Suggested daily macronutrients (" + goal.getProteinPercent() + "% protein, "
                + goal.getCarbohydratePercent() + "% carbohydrates, " + goal.getFatPercent() + "% fat):");
        view.println("  Protein        " + Fmt.padLeft(t.getProteinGrams() + " g", 6)
                + "   (" + Fmt.kcal(t.getProteinGrams() * 4.0) + " kcal, 4 kcal per gram)");
        view.println("  Carbohydrates  " + Fmt.padLeft(t.getCarbohydrateGrams() + " g", 6)
                + "   (" + Fmt.kcal(t.getCarbohydrateGrams() * 4.0) + " kcal, 4 kcal per gram)");
        view.println("  Fat            " + Fmt.padLeft(t.getFatGrams() + " g", 6)
                + "   (" + Fmt.kcal(t.getFatGrams() * 9.0) + " kcal, 9 kcal per gram)");

        view.blankLine();
        if (goal == HealthGoal.WEIGHT_LOSS
                && BMICalculator.evaluate(p.getWeightKg(), p.getHeightCm()).getCategory() == BmiCategory.UNDERWEIGHT) {
            view.info("Your BMI is in the underweight range, so losing weight is not recommended."
                    + " Please talk to a doctor or dietitian.");
        }
        if (p.getAge() < 18) {
            view.info("These formulas are designed for adults. Under 18, please check with a doctor or dietitian.");
        }
        view.printWrapped("", "Important: these numbers are estimates from standard formulas, not a medical"
                + " prescription. Needs vary from person to person; a doctor or registered dietitian"
                + " can give you personal advice.");
        view.blankLine();
        input.pause();
    }

    private static String signed(int value) {
        return value >= 0 ? "+ " + value : "- " + Math.abs(value);
    }
}
