package com.nutriplanner.service;

import com.nutriplanner.exception.DataStorageException;
import com.nutriplanner.exception.ValidationException;
import com.nutriplanner.model.User;
import com.nutriplanner.model.UserProfile;
import com.nutriplanner.storage.DataStorage;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Keeps all registered users and saves them through {@link DataStorage}.
 *
 * Usernames are stored in lower case, so "Priya" and "priya" are the same account.
 */
public class UserManager {

    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 64;

    private final DataStorage storage;
    private final PasswordHasher hasher;
    private final Clock clock;
    private final Map<String, User> users = new LinkedHashMap<>();

    public UserManager(DataStorage storage, PasswordHasher hasher, Clock clock) {
        this.storage = storage;
        this.hasher = hasher;
        this.clock = clock;
        for (User user : storage.loadUsers()) {
            users.putIfAbsent(normalizeUsername(user.getUsername()), user);
        }
    }

    /** Usernames are compared without spaces and without case. */
    public static String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    /** Checks the format of a new username. */
    public static void validateUsername(String username) {
        String name = normalizeUsername(username);
        if (name.isEmpty()) {
            throw new ValidationException("Username cannot be empty.");
        }
        if (name.length() < MIN_USERNAME_LENGTH || name.length() > MAX_USERNAME_LENGTH) {
            throw new ValidationException("Username must be between " + MIN_USERNAME_LENGTH + " and "
                    + MAX_USERNAME_LENGTH + " characters long.");
        }
        if (!name.matches("[a-z0-9_]+")) {
            throw new ValidationException("Username can contain only letters, numbers and underscores (_), with no spaces.");
        }
    }

    /** Checks that a password is strong enough. */
    public static void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password cannot be empty.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new ValidationException("Password must be between " + MIN_PASSWORD_LENGTH + " and "
                    + MAX_PASSWORD_LENGTH + " characters long.");
        }
        if (!password.trim().equals(password)) {
            throw new ValidationException("Password cannot start or end with a space.");
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            hasLetter |= Character.isLetter(c);
            hasDigit |= Character.isDigit(c);
        }
        if (!hasLetter || !hasDigit) {
            throw new ValidationException("Password must contain at least one letter and one number.");
        }
    }

    public boolean isUsernameTaken(String username) {
        return users.containsKey(normalizeUsername(username));
    }

    /**
     * Creates a new account and saves it.
     *
     * @throws ValidationException  when the username or password is not acceptable
     * @throws DataStorageException when the account cannot be saved
     */
    public User register(String username, String password, UserProfile profile) {
        validateUsername(username);
        validatePassword(password);
        if (profile == null) {
            throw new ValidationException("Profile details are required.");
        }
        String key = normalizeUsername(username);
        if (users.containsKey(key)) {
            throw new ValidationException("The username '" + key + "' is already taken. Please choose another one.");
        }
        User user = new User(key, hasher.hash(password), profile, LocalDate.now(clock));
        users.put(key, user);
        try {
            saveChanges();
        } catch (DataStorageException e) {
            users.remove(key);   // keep memory and file in step
            throw e;
        }
        return user;
    }

    public Optional<User> findUser(String username) {
        return Optional.ofNullable(users.get(normalizeUsername(username)));
    }

    /** Returns true when the password is the user's current password. */
    public boolean passwordMatches(User user, String password) {
        return hasher.verify(password, user.getPasswordHash());
    }

    /**
     * Changes a user's password after checking the current one.
     *
     * @throws ValidationException when the current password is wrong or the new one is weak
     */
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordMatches(user, currentPassword)) {
            throw new ValidationException("Your current password is not correct.");
        }
        validatePassword(newPassword);
        if (currentPassword.equals(newPassword)) {
            throw new ValidationException("The new password must be different from the current one.");
        }
        String oldHash = user.getPasswordHash();
        user.setPasswordHash(hasher.hash(newPassword));
        try {
            saveChanges();
        } catch (DataStorageException e) {
            user.setPasswordHash(oldHash);
            throw e;
        }
    }

    /** Saves every user to users.csv. Call this after changing a profile. */
    public void saveChanges() {
        storage.saveUsers(users.values());
    }

    public int getUserCount() {
        return users.size();
    }
}
