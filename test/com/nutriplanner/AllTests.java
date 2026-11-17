package com.nutriplanner;

/**
 * Runs every NutriPlanner test and prints a summary.
 *
 * From the project folder:
 *   javac -d out -sourcepath src src/com/nutriplanner/Main.java
 *   javac -d out -cp out -sourcepath test test/com/nutriplanner/AllTests.java
 *   java -cp out com.nutriplanner.AllTests
 */
public final class AllTests {

    private AllTests() {
    }

    public static void main(String[] args) throws Exception {
        TestSupport suite = new TestSupport();

        System.out.println("Calculators (BMI, BMR, TDEE, calorie targets)");
        CalculatorTests.run(suite);
        System.out.println("Food database");
        FoodDatabaseTests.run(suite);
        System.out.println("Users, login and passwords");
        UserTests.run(suite);
        System.out.println("Meal planner");
        MealPlannerTests.run(suite);
        System.out.println("Storage, tracker and reports");
        StorageAndReportTests.run(suite);
        System.out.println("Complete application runs (end to end)");
        EndToEndTests.run(suite);

        int failed = suite.getFailures().size();
        System.out.println();
        System.out.println("Tests run: " + (suite.getPassed() + failed) + ", passed: " + suite.getPassed()
                + ", failed: " + failed);
        if (failed > 0) {
            System.out.println("Failures:");
            for (String failure : suite.getFailures()) {
                System.out.println("  " + failure);
            }
            System.exit(1);
        }
        System.out.println("All tests passed.");
    }
}
