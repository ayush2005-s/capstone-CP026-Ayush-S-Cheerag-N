package com.nutriplanner.exception;

/**
 * Thrown when a login attempt fails because the username or password is wrong.
 */
public class AuthenticationException extends NutriPlannerException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }
}
