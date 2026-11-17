package com.nutriplanner.ui;

import com.nutriplanner.exception.ValidationException;
import com.nutriplanner.model.ActivityLevel;
import com.nutriplanner.model.DietType;
import com.nutriplanner.model.Gender;
import com.nutriplanner.model.HealthGoal;
import com.nutriplanner.model.UserProfile;
import com.nutriplanner.util.Fmt;

/**
 * Asks for profile details. Used both when registering (no current values)
 * and when updating a profile (pressing Enter keeps the current value).
 */
public class ProfileForm {

    private final ConsoleView view;
    private final ConsoleInput input;

    public ProfileForm(AppContext context) {
        this.view = context.getView();
        this.input = context.getInput();
    }

    /** Asks for every profile field and returns a new profile. */
    public UserProfile askNewProfile() {
        String name = askName(null);
        int age = askAge(null);
        Gender gender = input.chooseOption("Gender", Gender.values(), null);
        double height = askHeight(null);
        double weight = askWeight(null);
        ActivityLevel activity = input.chooseOption("Activity level", ActivityLevel.values(), null);
        DietType diet = input.chooseOption("Dietary preference", DietType.values(), null);
        HealthGoal goal = input.chooseOption("Health goal", HealthGoal.values(), null);
        return new UserProfile(name, age, gender, height, weight, activity, diet, goal);
    }

    /** Asks for every field again, showing the current values; Enter keeps a value. */
    public void updateAll(UserProfile profile) {
        view.println("Press Enter to keep the value shown in [brackets].");
        profile.setName(askName(profile.getName()));
        profile.setAge(askAge(profile.getAge()));
        profile.setGender(input.chooseOption("Gender", Gender.values(), profile.getGender()));
        profile.setHeightCm(askHeight(profile.getHeightCm()));
        profile.setWeightKg(askWeight(profile.getWeightKg()));
        profile.setActivityLevel(input.chooseOption("Activity level", ActivityLevel.values(), profile.getActivityLevel()));
        profile.setDietType(input.chooseOption("Dietary preference", DietType.values(), profile.getDietType()));
        profile.setHealthGoal(input.chooseOption("Health goal", HealthGoal.values(), profile.getHealthGoal()));
    }

    /** Full name; letters, spaces, dots, hyphens and apostrophes only. */
    public String askName(String current) {
        while (true) {
            String prompt = current == null ? "Full name: " : "Full name [" + current + "]: ";
            String text = input.readLine(prompt);
            if (text.isEmpty() && current != null) {
                return current;
            }
            try {
                return UserProfile.validateName(text);
            } catch (ValidationException e) {
                view.error(e.getMessage());
            }
        }
    }

    public int askAge(Integer current) {
        String range = "(" + UserProfile.MIN_AGE + "-" + UserProfile.MAX_AGE + ")";
        if (current == null) {
            return input.readInt("Age in years " + range + ": ", UserProfile.MIN_AGE, UserProfile.MAX_AGE);
        }
        Integer age = input.readOptionalInt("Age in years " + range + " [" + current + "]: ",
                UserProfile.MIN_AGE, UserProfile.MAX_AGE);
        return age == null ? current : age;
    }

    public double askHeight(Double current) {
        String prompt = "Height in cm (" + Fmt.number(UserProfile.MIN_HEIGHT_CM) + "-"
                + Fmt.number(UserProfile.MAX_HEIGHT_CM) + ")";
        if (current == null) {
            return input.readDouble(prompt + ": ", UserProfile.MIN_HEIGHT_CM, UserProfile.MAX_HEIGHT_CM);
        }
        Double height = input.readOptionalDouble(prompt + " [" + Fmt.oneDecimal(current) + "]: ",
                UserProfile.MIN_HEIGHT_CM, UserProfile.MAX_HEIGHT_CM);
        return height == null ? current : height;
    }

    public double askWeight(Double current) {
        String prompt = "Weight in kg (" + Fmt.number(UserProfile.MIN_WEIGHT_KG) + "-"
                + Fmt.number(UserProfile.MAX_WEIGHT_KG) + ")";
        if (current == null) {
            return input.readDouble(prompt + ": ", UserProfile.MIN_WEIGHT_KG, UserProfile.MAX_WEIGHT_KG);
        }
        Double weight = input.readOptionalDouble(prompt + " [" + Fmt.oneDecimal(current) + "]: ",
                UserProfile.MIN_WEIGHT_KG, UserProfile.MAX_WEIGHT_KG);
        return weight == null ? current : weight;
    }
}
