package com.nutriplanner;

import java.nio.file.Paths;
import java.time.Clock;

/**
 * Starts NutriPlanner, a terminal-based nutrition and diet planner.
 *
 * Compile and run from the project folder (the folder that contains "src"):
 *   javac -d out -sourcepath src src/com/nutriplanner/Main.java
 *   java -cp out com.nutriplanner.Main
 *
 * Data is saved in the "data" folder inside the folder you run it from.
 */
public class Main {

    public static void main(String[] args) {
        NutriPlannerApp app = new NutriPlannerApp(System.in, System.out, Paths.get("data"),
                Clock.systemDefaultZone(), true);
        try {
            app.run();
        } catch (RuntimeException e) {
            // Last line of defence. Ordinary input mistakes are handled inside the menus,
            // so reaching this point means something unexpected happened.
            System.out.println();
            System.out.println("An unexpected error stopped NutriPlanner: " + e);
            System.out.println("Everything saved before this point is safe in the 'data' folder.");
            System.out.println("Please start NutriPlanner again.");
        }
    }
}
