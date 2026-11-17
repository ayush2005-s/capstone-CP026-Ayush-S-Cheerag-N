package com.nutriplanner.ui;

import com.nutriplanner.exception.InputClosedException;
import com.nutriplanner.model.Labeled;
import com.nutriplanner.util.Fmt;

import java.io.Console;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

/**
 * Reads and checks everything the user types, using a Scanner.
 *
 * Each method keeps asking until the answer is valid and explains what was
 * wrong, so ordinary typing mistakes can never crash the program. When the
 * input ends (Ctrl+D or Ctrl+Z), an InputClosedException lets the program
 * close cleanly.
 */
public class ConsoleInput {

    private static final DateTimeFormatter DAY_FIRST =
            DateTimeFormatter.ofPattern("d-M-uuuu", Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter YEAR_FIRST =
            DateTimeFormatter.ofPattern("uuuu-M-d", Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT);

    private final Scanner scanner;
    private final ConsoleView view;
    private final boolean hidePasswordsWhenPossible;
    private boolean echoInput;

    /**
     * @param in                        where to read from (System.in, or a test script)
     * @param view                      where prompts and error messages are printed
     * @param hidePasswordsWhenPossible hide typed passwords when running in a real terminal
     */
    public ConsoleInput(InputStream in, ConsoleView view, boolean hidePasswordsWhenPossible) {
        this.scanner = new Scanner(in);
        this.view = view;
        this.hidePasswordsWhenPossible = hidePasswordsWhenPossible;
    }

    /** When true, every answer is printed after its prompt (used by automated tests). */
    public void setEchoInput(boolean echoInput) {
        this.echoInput = echoInput;
    }

    // ------------------------------------------------------------- text

    /** Reads one line and removes spaces at both ends. */
    public String readLine(String prompt) {
        return readRawLine(prompt).trim();
    }

    private String readRawLine(String prompt) {
        view.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        String line = scanner.nextLine();
        if (echoInput) {
            view.println(line);
        }
        return line;
    }

    /** Reads a line that may not be empty. */
    public String readRequired(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            view.error("This field cannot be empty. Please type a value.");
        }
    }

    /** True when passwords will be hidden while they are typed. */
    public boolean hidesPasswords() {
        Console console = hidePasswordsWhenPossible ? System.console() : null;
        return console != null && isInteractive(console);
    }

    /**
     * Reads a password. In a real terminal the typed characters are hidden;
     * otherwise (for example in some IDE consoles) the Scanner is used.
     */
    public String readPassword(String prompt) {
        if (hidesPasswords()) {
            view.print("");   // make sure earlier output is shown first
            char[] chars = System.console().readPassword("%s", prompt);
            if (chars == null) {
                throw new InputClosedException();
            }
            String password = new String(chars);
            Arrays.fill(chars, ' ');   // do not keep the password in memory longer than needed
            return password;
        }
        return readRawLine(prompt);
    }

    /**
     * Before Java 22, System.console() is null unless the program runs in a real terminal.
     * From Java 22 it can exist without one, so its isTerminal() method is checked as well.
     */
    private static boolean isInteractive(Console console) {
        try {
            Method isTerminal = Console.class.getMethod("isTerminal");
            return (Boolean) isTerminal.invoke(console);
        } catch (NoSuchMethodException e) {
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    // ---------------------------------------------------------- numbers

    /** Reads a whole number between min and max (inclusive). */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            Integer value = parseInt(readLine(prompt), min, max, false);
            if (value != null) {
                return value;
            }
        }
    }

    /** Like readInt, but pressing Enter returns null (meaning "keep the current value"). */
    public Integer readOptionalInt(String prompt, int min, int max) {
        while (true) {
            String text = readLine(prompt);
            if (text.isEmpty()) {
                return null;
            }
            Integer value = parseInt(text, min, max, true);
            if (value != null) {
                return value;
            }
        }
    }

    /** Reads a menu choice from 1 to optionCount. */
    public int readMenuChoice(int optionCount) {
        while (true) {
            String text = readLine("Enter your choice: ");
            if (text.isEmpty()) {
                view.error("Please type the number of an option from 1 to " + optionCount + ".");
                continue;
            }
            try {
                int choice = Integer.parseInt(text);
                if (choice >= 1 && choice <= optionCount) {
                    return choice;
                }
            } catch (NumberFormatException e) {
                // handled below
            }
            view.error("Invalid choice '" + text + "'. Please enter a number from 1 to " + optionCount + ".");
        }
    }

    private Integer parseInt(String text, int min, int max, boolean optional) {
        if (text.isEmpty()) {
            view.error("Please enter a value.");
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            view.error("'" + text + "' is not a whole number. Please use digits only, for example " + min + ".");
            return null;
        }
        if (value < min || value > max) {
            view.error("Please enter a number from " + min + " to " + max + "."
                    + (optional ? " Press Enter to keep the current value." : ""));
            return null;
        }
        return value;
    }

    /** Reads a decimal number between min and max. A comma is accepted as the decimal mark. */
    public double readDouble(String prompt, double min, double max) {
        while (true) {
            Double value = parseDouble(readLine(prompt), min, max);
            if (value != null) {
                return value;
            }
        }
    }

    /** Like readDouble, but pressing Enter returns null (meaning "keep the current value"). */
    public Double readOptionalDouble(String prompt, double min, double max) {
        while (true) {
            String text = readLine(prompt);
            if (text.isEmpty()) {
                return null;
            }
            Double value = parseDouble(text, min, max);
            if (value != null) {
                return value;
            }
        }
    }

    private Double parseDouble(String text, double min, double max) {
        if (text.isEmpty()) {
            view.error("Please enter a value.");
            return null;
        }
        double value;
        try {
            value = Double.parseDouble(text.replace(',', '.'));
        } catch (NumberFormatException e) {
            view.error("'" + text + "' is not a number. Please use digits, for example " + Fmt.number(min) + ".");
            return null;
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            view.error("Please enter an ordinary number.");
            return null;
        }
        if (value < 0) {
            view.error("The value cannot be negative.");
            return null;
        }
        if (value < min || value > max) {
            view.error("Please enter a value from " + Fmt.number(min) + " to " + Fmt.number(max) + ".");
            return null;
        }
        return value;
    }

    // ---------------------------------------------------------- choices

    /** Asks a yes/no question. Accepts y, yes, n or no in any case. */
    public boolean readYesNo(String question) {
        while (true) {
            String answer = readLine(question + " (y/n): ").toLowerCase(Locale.ROOT);
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            view.error("Please answer y for yes or n for no.");
        }
    }

    /**
     * Shows numbered options and returns the one chosen.
     * When current is not null, pressing Enter keeps it.
     */
    public <E extends Labeled> E chooseOption(String title, E[] options, E current) {
        view.println(title + ":");
        int numberWidth = String.valueOf(options.length).length();
        for (int i = 0; i < options.length; i++) {
            E option = options[i];
            String description = option.getDescription().isEmpty() ? "" : " - " + option.getDescription();
            String marker = option == current ? "   (current)" : "";
            view.println("  " + Fmt.padLeft(String.valueOf(i + 1), numberWidth) + ". " + option.getLabel()
                    + description + marker);
        }
        if (current == null) {
            return options[readInt("Enter your choice (1-" + options.length + "): ", 1, options.length) - 1];
        }
        Integer choice = readOptionalInt("Enter your choice (1-" + options.length
                + "), or press Enter to keep " + current.getLabel() + ": ", 1, options.length);
        return choice == null ? current : options[choice - 1];
    }

    // ------------------------------------------------------------ dates

    /**
     * Reads a date such as 17-11-2026. Also accepts "today", "yesterday" and
     * "tomorrow". Pressing Enter means today. Dates more than 5 years ago or
     * more than 1 year ahead are refused.
     */
    public LocalDate readDate(String prompt, LocalDate today) {
        LocalDate earliest = today.minusYears(5);
        LocalDate latest = today.plusYears(1);
        while (true) {
            String text = readLine(prompt).toLowerCase(Locale.ROOT);
            LocalDate date;
            if (text.isEmpty() || text.equals("today")) {
                date = today;
            } else if (text.equals("yesterday")) {
                date = today.minusDays(1);
            } else if (text.equals("tomorrow")) {
                date = today.plusDays(1);
            } else {
                date = parseDate(text);
                if (date == null) {
                    view.error("'" + text + "' is not a valid date. Please use DD-MM-YYYY, for example "
                            + Fmt.inputDate(today) + ".");
                    continue;
                }
            }
            if (date.isBefore(earliest) || date.isAfter(latest)) {
                view.error("Please choose a date between " + Fmt.inputDate(earliest) + " and "
                        + Fmt.inputDate(latest) + ".");
                continue;
            }
            return date;
        }
    }

    private static LocalDate parseDate(String text) {
        String normalized = text.replace('/', '-').replace('.', '-');
        for (DateTimeFormatter format : new DateTimeFormatter[] {DAY_FIRST, YEAR_FIRST}) {
            try {
                return LocalDate.parse(normalized, format);
            } catch (DateTimeParseException e) {
                // try the next format
            }
        }
        return null;
    }

    /** Waits until the user presses Enter, so they can read the output first. */
    public void pause() {
        readRawLine("Press Enter to continue...");
    }
}
