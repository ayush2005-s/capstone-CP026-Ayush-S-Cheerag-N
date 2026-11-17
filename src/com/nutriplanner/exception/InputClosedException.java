package com.nutriplanner.exception;

/**
 * Thrown when there is no more input to read, for example when the user
 * presses Ctrl+D (Mac/Linux) or Ctrl+Z then Enter (Windows).
 * The application catches it and closes cleanly instead of looping forever.
 */
public class InputClosedException extends NutriPlannerException {

    private static final long serialVersionUID = 1L;

    public InputClosedException() {
        super("No more input is available.");
    }
}
