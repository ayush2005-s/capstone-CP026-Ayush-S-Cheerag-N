package com.nutriplanner.exception;

/**
 * Base class for every error that NutriPlanner raises on purpose.
 *
 * All custom exceptions extend this class (inheritance), so a menu can catch
 * them in one place and show a friendly message instead of crashing.
 */
public class NutriPlannerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NutriPlannerException(String message) {
        super(message);
    }

    public NutriPlannerException(String message, Throwable cause) {
        super(message, cause);
    }
}
