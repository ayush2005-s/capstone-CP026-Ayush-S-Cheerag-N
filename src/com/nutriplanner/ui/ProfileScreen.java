package com.nutriplanner.ui;

import com.nutriplanner.model.BmiResult;
import com.nutriplanner.model.NutritionTargets;
import com.nutriplanner.model.User;
import com.nutriplanner.model.UserProfile;
import com.nutriplanner.service.BMICalculator;
import com.nutriplanner.service.NutritionCalculator;
import com.nutriplanner.util.Fmt;

/**
 * Shows the logged-in user's profile.
 */
public class ProfileScreen {

    private final ConsoleView view;
    private final ConsoleInput input;
    private final User user;

    public ProfileScreen(AppContext context, User user) {
        this.view = context.getView();
        this.input = context.getInput();
        this.user = user;
    }

    public void show() {
        UserProfile p = user.getProfile();
        view.blankLine();
        view.header("YOUR PROFILE");
        view.field("Username", user.getUsername());
        view.field("Name", p.getName());
        view.field("Age", p.getAge() + " years");
        view.field("Gender", p.getGender().getLabel());
        view.field("Height", Fmt.oneDecimal(p.getHeightCm()) + " cm");
        view.field("Weight", Fmt.oneDecimal(p.getWeightKg()) + " kg");
        view.field("Activity level", p.getActivityLevel().getLabel() + " (" + p.getActivityLevel().getDescription() + ")");
        view.field("Dietary preference", p.getDietType().getLabel());
        view.field("Health goal", p.getHealthGoal().getLabel());
        view.field("Member since", Fmt.shortDate(user.getCreatedOn()));

        BmiResult bmi = BMICalculator.evaluate(p.getWeightKg(), p.getHeightCm());
        NutritionTargets targets = NutritionCalculator.calculateTargets(p);
        view.rule('-');
        view.field("BMI", Fmt.oneDecimal(bmi.getBmi()) + " (" + bmi.getCategory().getLabel() + ")");
        view.field("Daily calorie target", Fmt.kcal(targets.getCalorieTarget()) + " kcal");
        view.blankLine();
        input.pause();
    }
}
