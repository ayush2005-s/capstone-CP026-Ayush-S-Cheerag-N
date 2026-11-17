package com.nutriplanner;

import com.nutriplanner.exception.DataStorageException;
import com.nutriplanner.exception.InputClosedException;
import com.nutriplanner.service.Authentication;
import com.nutriplanner.service.FoodDatabase;
import com.nutriplanner.service.MealPlanner;
import com.nutriplanner.service.NutritionReport;
import com.nutriplanner.service.NutritionTracker;
import com.nutriplanner.service.PasswordHasher;
import com.nutriplanner.service.UserManager;
import com.nutriplanner.storage.DataStorage;
import com.nutriplanner.ui.AppContext;
import com.nutriplanner.ui.ConsoleInput;
import com.nutriplanner.ui.ConsoleView;
import com.nutriplanner.ui.WelcomeMenu;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Clock;

/**
 * Creates all the objects NutriPlanner needs, loads the saved data and starts
 * the welcome menu.
 *
 * Input, output, data folder and clock are passed in, so the automated tests
 * can run the whole application with a scripted input and a temporary folder.
 */
public class NutriPlannerApp {

    private final InputStream in;
    private final PrintStream out;
    private final Path dataDirectory;
    private final Clock clock;
    private final boolean hidePasswords;
    private boolean echoInput;

    public NutriPlannerApp(InputStream in, PrintStream out, Path dataDirectory, Clock clock, boolean hidePasswords) {
        this.in = in;
        this.out = out;
        this.dataDirectory = dataDirectory;
        this.clock = clock;
        this.hidePasswords = hidePasswords;
    }

    /** Prints every typed answer after its prompt; used when input comes from a script. */
    public void setEchoInput(boolean echoInput) {
        this.echoInput = echoInput;
    }

    public void run() {
        ConsoleView view = new ConsoleView(out);
        ConsoleInput input = new ConsoleInput(in, view, hidePasswords);
        input.setEchoInput(echoInput);
        try {
            // 1. Load saved data (missing files simply mean "no data yet").
            DataStorage storage = new DataStorage(dataDirectory);
            PasswordHasher hasher = new PasswordHasher();
            UserManager userManager = new UserManager(storage, hasher, clock);
            FoodDatabase foodDatabase = new FoodDatabase(storage);
            MealPlanner mealPlanner = new MealPlanner(storage);

            // 2. Create the services that work with that data.
            Authentication authentication = new Authentication(userManager, hasher);
            NutritionTracker tracker = new NutritionTracker();
            NutritionReport report = new NutritionReport(tracker);
            AppContext context = new AppContext(view, input, storage, userManager, authentication,
                    foodDatabase, mealPlanner, tracker, report, clock);

            // 3. Say where the data lives, report damaged lines that were skipped, then start the menus.
            view.println("Data folder: " + storage.getDataDirectory().toAbsolutePath().normalize());
            for (String warning : storage.getWarnings()) {
                view.info(warning);
            }
            new WelcomeMenu(context).run();

            view.blankLine();
            view.println("Thank you for using NutriPlanner. Your data has been saved. Goodbye!");
        } catch (InputClosedException e) {
            view.blankLine();
            view.println("Input ended, so NutriPlanner has closed. Your data has been saved.");
        } catch (DataStorageException e) {
            view.blankLine();
            view.error("NutriPlanner cannot use its data folder: " + e.getMessage()
                    + (e.getCause() != null ? " (" + e.getCause().getMessage() + ")" : ""));
            view.println("Check that you can create files in this folder, then start NutriPlanner again.");
        }
    }
}
