package com.nutriplanner.model;

import java.time.LocalDate;

/**
 * A registered user: login details plus a {@link UserProfile}.
 *
 * A User "has a" profile (composition). The password itself is never stored;
 * only a salted hash of it is kept (see PasswordHasher).
 */
public class User {

    private final String username;
    private String passwordHash;
    private final UserProfile profile;
    private final LocalDate createdOn;

    public User(String username, String passwordHash, UserProfile profile, LocalDate createdOn) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.profile = profile;
        this.createdOn = createdOn;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public LocalDate getCreatedOn() {
        return createdOn;
    }

    /** The user's real name, used in greetings. */
    public String getDisplayName() {
        return profile.getName();
    }
}
