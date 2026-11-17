package com.nutriplanner.exception;

/**
 * Thrown when NutriPlanner cannot read or write its data files,
 * for example when the disk is full or the folder is read-only.
 */
public class DataStorageException extends NutriPlannerException {

    private static final long serialVersionUID = 1L;

    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
