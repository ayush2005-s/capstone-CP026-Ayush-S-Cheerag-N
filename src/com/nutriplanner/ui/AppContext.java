package com.nutriplanner.ui;

import com.nutriplanner.service.Authentication;
import com.nutriplanner.service.FoodDatabase;
import com.nutriplanner.service.MealPlanner;
import com.nutriplanner.service.NutritionReport;
import com.nutriplanner.service.NutritionTracker;
import com.nutriplanner.service.UserManager;
import com.nutriplanner.storage.DataStorage;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Holds the objects that every screen needs: input, output and the services.
 * It is created once in NutriPlannerApp and passed to each menu, so no screen
 * has to create its own services (this keeps all data in one place).
 */
public class AppContext {

    private final ConsoleView view;
    private final ConsoleInput input;
    private final DataStorage storage;
    private final UserManager userManager;
    private final Authentication authentication;
    private final FoodDatabase foodDatabase;
    private final MealPlanner mealPlanner;
    private final NutritionTracker tracker;
    private final NutritionReport report;
    private final Clock clock;

    public AppContext(ConsoleView view, ConsoleInput input, DataStorage storage, UserManager userManager,
                      Authentication authentication, FoodDatabase foodDatabase, MealPlanner mealPlanner,
                      NutritionTracker tracker, NutritionReport report, Clock clock) {
        this.view = view;
        this.input = input;
        this.storage = storage;
        this.userManager = userManager;
        this.authentication = authentication;
        this.foodDatabase = foodDatabase;
        this.mealPlanner = mealPlanner;
        this.tracker = tracker;
        this.report = report;
        this.clock = clock;
    }

    public ConsoleView getView() {
        return view;
    }

    public ConsoleInput getInput() {
        return input;
    }

    public DataStorage getStorage() {
        return storage;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public FoodDatabase getFoodDatabase() {
        return foodDatabase;
    }

    public MealPlanner getMealPlanner() {
        return mealPlanner;
    }

    public NutritionTracker getTracker() {
        return tracker;
    }

    public NutritionReport getReport() {
        return report;
    }

    /** Today's date. A Clock is used so tests can fix the date. */
    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
