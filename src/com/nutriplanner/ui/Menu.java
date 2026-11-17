package com.nutriplanner.ui;

import com.nutriplanner.exception.AuthenticationException;
import com.nutriplanner.exception.DataStorageException;
import com.nutriplanner.exception.InputClosedException;
import com.nutriplanner.exception.ValidationException;
import com.nutriplanner.util.Fmt;

/**
 * Base class of every menu screen (abstraction and the "template method" pattern).
 *
 * run() contains the loop that all menus share: print the title, print the
 * numbered options, read a valid choice and handle it. Each subclass only says
 * what its title and options are and what each choice does, by overriding the
 * abstract methods (inheritance and polymorphism).
 */
public abstract class Menu {

    protected final AppContext context;
    protected final ConsoleView view;
    protected final ConsoleInput input;

    protected Menu(AppContext context) {
        this.context = context;
        this.view = context.getView();
        this.input = context.getInput();
    }

    /** The heading shown at the top of the menu. */
    protected abstract String getTitle();

    /** The option texts, shown as 1, 2, 3 ... */
    protected abstract String[] getOptions();

    /**
     * Carries out the chosen option.
     *
     * @param choice a number from 1 to the number of options
     * @return true to show this menu again, false to leave it
     */
    protected abstract boolean handleChoice(int choice);

    /** Prints the heading. Subclasses may override it, as the welcome screen does. */
    protected void printHeader() {
        view.header(getTitle());
    }

    /** Optional extra lines shown under the heading, such as the selected date. */
    protected void printContext() {
        // nothing by default
    }

    /** Shows the menu until the user chooses to leave it. */
    public final void run() {
        boolean keepRunning = true;
        while (keepRunning) {
            view.blankLine();
            printHeader();
            printContext();
            String[] options = getOptions();
            int numberWidth = String.valueOf(options.length).length();
            for (int i = 0; i < options.length; i++) {
                // Right-align the numbers so options 9 and 10 line up.
                view.println(Fmt.padLeft(String.valueOf(i + 1), numberWidth) + ". " + options[i]);
            }
            view.blankLine();
            int choice = input.readMenuChoice(options.length);
            try {
                keepRunning = handleChoice(choice);
            } catch (InputClosedException e) {
                throw e;   // no more input: let the application close
            } catch (ValidationException | AuthenticationException e) {
                view.error(e.getMessage());
            } catch (DataStorageException e) {
                view.error("Your change could not be saved. " + e.getMessage()
                        + (e.getCause() != null ? " (" + e.getCause().getMessage() + ")" : ""));
            } catch (RuntimeException e) {
                // Unexpected problem: report it and return to the menu instead of crashing.
                view.error("Something unexpected went wrong (" + e + "). Please try again.");
            }
        }
    }
}
