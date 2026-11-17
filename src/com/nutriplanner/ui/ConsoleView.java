package com.nutriplanner.ui;

import com.nutriplanner.util.Fmt;

import java.io.PrintStream;

/**
 * All terminal output goes through this class, so every screen uses the same
 * headings, spacing and message style.
 */
public class ConsoleView {

    public static final int WIDTH = Fmt.LINE_WIDTH;
    private static final int LABEL_WIDTH = 22;

    private final PrintStream out;

    public ConsoleView(PrintStream out) {
        this.out = out;
    }

    public void println(String text) {
        out.println(text);
    }

    public void blankLine() {
        out.println();
    }

    /** Prints text without a new line (used for prompts). */
    public void print(String text) {
        out.print(text);
        out.flush();
    }

    /** Prints text that may already contain several lines, such as a table or report. */
    public void printBlock(String text) {
        out.print(text);
        if (!text.endsWith("\n")) {
            out.println();
        }
        out.flush();
    }

    /** The application title shown on the welcome screen. */
    public void banner() {
        rule('=');
        out.println(Fmt.center("NUTRIPLANNER", WIDTH));
        out.println(Fmt.center("YOUR PERSONAL NUTRITION PLANNER", WIDTH));
        rule('=');
    }

    /** A screen title between two lines of "=". */
    public void header(String title) {
        rule('=');
        out.println(Fmt.center(title, WIDTH));
        rule('=');
    }

    /** A smaller heading inside a screen. */
    public void section(String title) {
        out.println();
        out.println(title);
        rule('-');
    }

    public void rule(char c) {
        out.println(Fmt.repeat(c, WIDTH));
    }

    /** A "label : value" line with the labels lined up. */
    public void field(String label, String value) {
        out.println(Fmt.padRight(label, LABEL_WIDTH) + ": " + value);
    }

    /** A confirmation such as "Profile saved.", wrapped to the screen width. */
    public void success(String message) {
        printWrapped("", message);
    }

    /** Extra information, shown as "Note: ...". */
    public void info(String message) {
        printWrapped("Note: ", message);
    }

    /** A problem the user can fix, shown as "Error: ...". */
    public void error(String message) {
        printWrapped("Error: ", message);
    }

    /** Prints a long message over several lines so it is not cut off by the terminal. */
    public void printWrapped(String prefix, String message) {
        for (String line : Fmt.wrap(prefix, message, WIDTH)) {
            out.println(line);
        }
    }
}
