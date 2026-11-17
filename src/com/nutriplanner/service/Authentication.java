package com.nutriplanner.service;

import com.nutriplanner.exception.AuthenticationException;
import com.nutriplanner.model.User;

import java.util.Optional;

/**
 * Handles login and logout and remembers who is logged in.
 */
public class Authentication {

    private final UserManager userManager;
    private final PasswordHasher hasher;
    private final String dummyHash;
    private User currentUser;

    public Authentication(UserManager userManager, PasswordHasher hasher) {
        this.userManager = userManager;
        this.hasher = hasher;
        // Used when the username does not exist, so a wrong username takes as long
        // to reject as a wrong password and does not reveal which accounts exist.
        this.dummyHash = hasher.hash("not-a-real-password-1");
    }

    /**
     * Logs a user in.
     *
     * @return the logged-in user
     * @throws AuthenticationException when the username or password is wrong
     */
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new AuthenticationException("Please enter both your username and your password.");
        }
        Optional<User> user = userManager.findUser(username);
        String storedHash = user.isPresent() ? user.get().getPasswordHash() : dummyHash;
        boolean passwordMatches = hasher.verify(password, storedHash);
        if (!user.isPresent() || !passwordMatches) {
            // The same message for both cases, so attackers cannot learn which usernames exist.
            throw new AuthenticationException("Invalid username or password.");
        }
        currentUser = user.get();
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
