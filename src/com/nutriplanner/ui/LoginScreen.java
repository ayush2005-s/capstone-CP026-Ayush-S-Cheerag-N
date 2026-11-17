package com.nutriplanner.ui;

import com.nutriplanner.exception.AuthenticationException;
import com.nutriplanner.model.User;
import com.nutriplanner.service.Authentication;

/**
 * Asks for a username and password. After three wrong attempts the user is
 * sent back to the welcome screen.
 */
public class LoginScreen {

    public static final int MAX_ATTEMPTS = 3;

    private final ConsoleView view;
    private final ConsoleInput input;
    private final Authentication authentication;

    public LoginScreen(AppContext context) {
        this.view = context.getView();
        this.input = context.getInput();
        this.authentication = context.getAuthentication();
    }

    /** Returns the logged-in user, or null when the login was cancelled or failed. */
    public User show() {
        view.blankLine();
        view.header("LOGIN");
        view.println("Press Enter without typing a username to go back.");
        if (input.hidesPasswords()) {
            view.println("For your privacy, the password is not shown while you type it.");
        }
        view.blankLine();
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String username = input.readLine("Username: ");
            if (username.isEmpty()) {
                return null;
            }
            String password = input.readPassword("Password: ");
            try {
                User user = authentication.login(username, password);
                view.blankLine();
                view.success("Login successful. Welcome back, " + user.getDisplayName() + "!");
                return user;
            } catch (AuthenticationException e) {
                view.error(e.getMessage());
                int left = MAX_ATTEMPTS - attempt;
                if (left > 0) {
                    view.println("You have " + left + " attempt" + (left == 1 ? "" : "s") + " left.");
                }
            }
        }
        view.error("Too many failed attempts. Returning to the welcome screen.");
        return null;
    }
}
