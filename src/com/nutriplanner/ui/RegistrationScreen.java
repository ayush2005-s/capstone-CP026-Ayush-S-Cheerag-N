package com.nutriplanner.ui;

import com.nutriplanner.exception.ValidationException;
import com.nutriplanner.model.User;
import com.nutriplanner.model.UserProfile;
import com.nutriplanner.service.UserManager;

/**
 * Creates a new account: username, password and profile details.
 */
public class RegistrationScreen {

    private final AppContext context;
    private final ConsoleView view;
    private final ConsoleInput input;
    private final UserManager userManager;

    public RegistrationScreen(AppContext context) {
        this.context = context;
        this.view = context.getView();
        this.input = context.getInput();
        this.userManager = context.getUserManager();
    }

    public void show() {
        view.blankLine();
        view.header("REGISTER A NEW ACCOUNT");
        view.println("Press Enter without typing a username to go back.");
        view.blankLine();

        String username = askUsername();
        if (username == null) {
            view.println("Registration cancelled.");
            return;
        }
        String password = askNewPassword("Password", "Confirm password");

        view.section("YOUR PROFILE");
        view.println("These details are used to calculate your BMI and daily calorie needs.");
        UserProfile profile = new ProfileForm(context).askNewProfile();

        User user = userManager.register(username, password, profile);
        view.blankLine();
        view.success("Registration successful. Welcome to NutriPlanner, " + user.getDisplayName() + "!");
        view.println("Please choose Login and sign in with the username '" + user.getUsername() + "'.");
        input.pause();
    }

    /** Returns a valid, unused username, or null when the user presses Enter to cancel. */
    private String askUsername() {
        while (true) {
            String username = input.readLine("Username (3-20 letters, numbers or _): ");
            if (username.isEmpty()) {
                return null;
            }
            try {
                UserManager.validateUsername(username);
                if (userManager.isUsernameTaken(username)) {
                    view.error("The username '" + UserManager.normalizeUsername(username)
                            + "' is already taken. Please choose another one.");
                    continue;
                }
                return UserManager.normalizeUsername(username);
            } catch (ValidationException e) {
                view.error(e.getMessage());
            }
        }
    }

    /** Asks for a new password twice until both entries match and the password is strong enough. */
    String askNewPassword(String label, String confirmLabel) {
        view.println("Passwords need at least " + UserManager.MIN_PASSWORD_LENGTH
                + " characters, including a letter and a number.");
        if (input.hidesPasswords()) {
            view.println("For your privacy, the password is not shown while you type it.");
        }
        while (true) {
            String password = input.readPassword(label + ": ");
            try {
                UserManager.validatePassword(password);
            } catch (ValidationException e) {
                view.error(e.getMessage());
                continue;
            }
            String confirmation = input.readPassword(confirmLabel + ": ");
            if (password.equals(confirmation)) {
                return password;
            }
            view.error("The two passwords do not match. Please try again.");
        }
    }
}
