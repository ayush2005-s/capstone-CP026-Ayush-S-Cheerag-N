package com.nutriplanner.exception;

/**
 * Thrown when a value breaks a rule, for example a negative weight,
 * an age outside the allowed range or a username that is already taken.
 */
public class ValidationException extends NutriPlannerException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
