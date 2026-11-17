package com.nutriplanner.ui;

import com.nutriplanner.model.User;

/**
 * The main menu shown after logging in.
 */
public class MainMenu extends Menu {

    private final User user;

    public MainMenu(AppContext context, User user) {
        super(context);
        this.user = user;
    }

    @Override
    protected String getTitle() {
        return "MAIN MENU";
    }

    @Override
    protected void printContext() {
        view.println("Logged in as: " + user.getDisplayName() + " (" + user.getUsername() + ")");
        view.blankLine();
    }

    @Override
    protected String[] getOptions() {
        return new String[] {
            "View Profile",
            "Update Profile",
            "Calculate BMI",
            "Calculate Daily Calorie Requirements",
            "Food Database",
            "Meal Planner",
            "Daily Nutrition Tracker",
            "View Nutrition Report",
            "Logout"
        };
    }

    @Override
    protected boolean handleChoice(int choice) {
        switch (choice) {
            case 1:
                new ProfileScreen(context, user).show();
                break;
            case 2:
                new UpdateProfileMenu(context, user).run();
                break;
            case 3:
                new BmiMenu(context, user).run();
                break;
            case 4:
                new CalorieScreen(context, user).show();
                break;
            case 5:
                new FoodDatabaseMenu(context, user).run();
                break;
            case 6:
                new MealPlannerMenu(context, user).run();
                break;
            case 7:
                new TrackerMenu(context, user).run();
                break;
            case 8:
                new ReportMenu(context, user).run();
                break;
            default:
                context.getAuthentication().logout();
                view.blankLine();
                view.success("You have been logged out. See you soon, " + user.getDisplayName() + "!");
                return false;
        }
        return true;
    }
}
