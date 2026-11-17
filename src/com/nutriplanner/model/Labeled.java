package com.nutriplanner.model;

/**
 * Something that has a human-readable label, such as an option in a menu.
 *
 * The enums in this package implement this interface, so the terminal
 * interface can print any of them as a numbered list with one method.
 */
public interface Labeled {

    /** Short name shown to the user, for example "Lightly active". */
    String getLabel();

    /** Optional extra explanation shown next to the label. */
    default String getDescription() {
        return "";
    }
}
