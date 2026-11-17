package com.nutriplanner.ui;

import com.nutriplanner.model.User;

/**
 * The first screen: register, log in or exit.
 */
public class WelcomeMenu extends Menu {

    public WelcomeMenu(AppContext context) {
        super(context);
    }

    @Override
    protected String getTitle() {
        return "NUTRIPLANNER";
    }

    @Override
    protected void printHeader() {
        view.banner();   // the big title instead of a normal heading
    }

    @Override
    protected String[] getOptions() {
        return new String[] {"Register", "Login", "Exit"};
    }

    @Override
    protected boolean handleChoice(int choice) {
        switch (choice) {
            case 1:
                new RegistrationScreen(context).show();
                return true;
            case 2:
                User user = new LoginScreen(context).show();
                if (user != null) {
                    new MainMenu(context, user).run();
                }
                return true;
            default:
                return false;   // Exit
        }
    }
}
