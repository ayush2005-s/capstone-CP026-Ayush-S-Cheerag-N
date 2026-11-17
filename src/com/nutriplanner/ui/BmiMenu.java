package com.nutriplanner.ui;

import com.nutriplanner.model.BmiCategory;
import com.nutriplanner.model.BmiResult;
import com.nutriplanner.model.User;
import com.nutriplanner.model.UserProfile;
import com.nutriplanner.service.BMICalculator;
import com.nutriplanner.util.Fmt;

/**
 * BMI calculator: for the user's own profile or for any height and weight.
 */
public class BmiMenu extends Menu {

    private final User user;

    public BmiMenu(AppContext context, User user) {
        super(context);
        this.user = user;
    }

    @Override
    protected String getTitle() {
        return "BMI CALCULATOR";
    }

    @Override
    protected void printContext() {
        view.println("Formula: BMI = weight (kg) / height (m) x height (m)");
        view.blankLine();
    }

    @Override
    protected String[] getOptions() {
        UserProfile p = user.getProfile();
        return new String[] {
            "Calculate my BMI (" + Fmt.oneDecimal(p.getHeightCm()) + " cm, " + Fmt.oneDecimal(p.getWeightKg()) + " kg)",
            "Calculate BMI for another height and weight",
            "Back to main menu"
        };
    }

    @Override
    protected boolean handleChoice(int choice) {
        switch (choice) {
            case 1:
                UserProfile p = user.getProfile();
                showResult(BMICalculator.evaluate(p.getWeightKg(), p.getHeightCm()), p.getAge() < 18);
                return true;
            case 2:
                view.blankLine();
                double height = input.readDouble("Height in cm (" + Fmt.number(UserProfile.MIN_HEIGHT_CM) + "-"
                        + Fmt.number(UserProfile.MAX_HEIGHT_CM) + "): ",
                        UserProfile.MIN_HEIGHT_CM, UserProfile.MAX_HEIGHT_CM);
                double weight = input.readDouble("Weight in kg (" + Fmt.number(UserProfile.MIN_WEIGHT_KG) + "-"
                        + Fmt.number(UserProfile.MAX_WEIGHT_KG) + "): ",
                        UserProfile.MIN_WEIGHT_KG, UserProfile.MAX_WEIGHT_KG);
                showResult(BMICalculator.evaluate(weight, height), false);
                return true;
            default:
                return false;
        }
    }

    private void showResult(BmiResult result, boolean underEighteen) {
        double heightM = result.getHeightCm() / 100.0;
        view.section("BMI RESULT");
        view.field("Height", Fmt.oneDecimal(result.getHeightCm()) + " cm (" + Fmt.number(heightM) + " m)");
        view.field("Weight", Fmt.oneDecimal(result.getWeightKg()) + " kg");
        view.field("Calculation", Fmt.oneDecimal(result.getWeightKg()) + " / (" + Fmt.number(heightM) + " x "
                + Fmt.number(heightM) + ") = " + Fmt.oneDecimal(result.getBmi()));
        view.field("Your BMI", Fmt.oneDecimal(result.getBmi()));
        view.field("Category", result.getCategory().getLabel());
        view.blankLine();
        view.println("What this means:");
        view.printWrapped("  ", result.getCategory().getExplanation());
        view.blankLine();
        view.println("BMI categories for adults (World Health Organization):");
        for (BmiCategory category : BmiCategory.values()) {
            String marker = category == result.getCategory() ? "   <-- you" : "";
            view.println("  " + Fmt.padRight(category.getRangeText(), 16) + category.getLabel() + marker);
        }
        view.blankLine();
        view.field("Healthy weight range", Fmt.oneDecimal(result.getHealthyMinWeightKg()) + " - "
                + Fmt.oneDecimal(result.getHealthyMaxWeightKg()) + " kg for this height");
        view.blankLine();
        view.info("BMI does not tell muscle from fat, so very muscular people can show a high BMI.");
        view.info("For South Asian adults, health risks start to rise at a lower BMI (around 23).");
        if (underEighteen) {
            view.info("For people under 18, doctors use age-based BMI charts instead of these categories.");
        }
        view.blankLine();
        input.pause();
    }
}
