package com.nutriplanner.ui;

import com.nutriplanner.model.ActivityLevel;
import com.nutriplanner.model.DietType;
import com.nutriplanner.model.Gender;
import com.nutriplanner.model.HealthGoal;
import com.nutriplanner.model.NutritionTargets;
import com.nutriplanner.model.User;
import com.nutriplanner.model.UserProfile;
import com.nutriplanner.service.NutritionCalculator;
import com.nutriplanner.util.Fmt;

/**
 * Lets the user change one profile detail at a time, all details at once,
 * or their password. Every change is saved immediately.
 */
public class UpdateProfileMenu extends Menu {

    private final User user;
    private final ProfileForm form;

    public UpdateProfileMenu(AppContext context, User user) {
        super(context);
        this.user = user;
        this.form = new ProfileForm(context);
    }

    @Override
    protected String getTitle() {
        return "UPDATE PROFILE";
    }

    @Override
    protected void printContext() {
        view.println("What would you like to change?");
        view.blankLine();
    }

    @Override
    protected String[] getOptions() {
        UserProfile p = user.getProfile();
        return new String[] {
            "Name                [" + p.getName() + "]",
            "Age                 [" + p.getAge() + " years]",
            "Gender              [" + p.getGender().getLabel() + "]",
            "Height              [" + Fmt.oneDecimal(p.getHeightCm()) + " cm]",
            "Weight              [" + Fmt.oneDecimal(p.getWeightKg()) + " kg]",
            "Activity level      [" + p.getActivityLevel().getLabel() + "]",
            "Dietary preference  [" + p.getDietType().getLabel() + "]",
            "Health goal         [" + p.getHealthGoal().getLabel() + "]",
            "Update all details",
            "Change password",
            "Back to main menu"
        };
    }

    @Override
    protected boolean handleChoice(int choice) {
        UserProfile p = user.getProfile();
        if (choice == 11) {
            return false;   // Back to main menu
        }
        view.blankLine();
        switch (choice) {
            case 1:
                p.setName(form.askName(p.getName()));
                break;
            case 2:
                p.setAge(form.askAge(p.getAge()));
                break;
            case 3:
                p.setGender(input.chooseOption("Gender", Gender.values(), p.getGender()));
                break;
            case 4:
                p.setHeightCm(form.askHeight(p.getHeightCm()));
                break;
            case 5:
                p.setWeightKg(form.askWeight(p.getWeightKg()));
                break;
            case 6:
                p.setActivityLevel(input.chooseOption("Activity level", ActivityLevel.values(), p.getActivityLevel()));
                break;
            case 7:
                p.setDietType(input.chooseOption("Dietary preference", DietType.values(), p.getDietType()));
                break;
            case 8:
                p.setHealthGoal(input.chooseOption("Health goal", HealthGoal.values(), p.getHealthGoal()));
                break;
            case 9:
                form.updateAll(p);
                break;
            default:
                changePassword();   // option 10
                return true;
        }
        saveProfile();
        return true;
    }

    private void saveProfile() {
        context.getUserManager().saveChanges();
        NutritionTargets targets = NutritionCalculator.calculateTargets(user.getProfile());
        view.blankLine();
        view.success("Profile saved. Your daily calorie target is now " + Fmt.kcal(targets.getCalorieTarget()) + " kcal.");
    }

    private void changePassword() {
        view.section("CHANGE PASSWORD");
        String current = input.readPassword("Current password: ");
        if (!context.getUserManager().passwordMatches(user, current)) {
            view.error("Your current password is not correct. Your password was not changed.");
            return;
        }
        String newPassword = new RegistrationScreen(context).askNewPassword("New password", "Confirm new password");
        context.getUserManager().changePassword(user, current, newPassword);
        view.success("Your password has been changed.");
    }
}
