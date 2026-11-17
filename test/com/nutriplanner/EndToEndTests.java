package com.nutriplanner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static com.nutriplanner.TestSupport.assertContains;
import static com.nutriplanner.TestSupport.assertNotContains;
import static com.nutriplanner.TestSupport.check;

/**
 * Runs the complete terminal application with typed answers from a script,
 * exactly as a user would, including many invalid answers. Then it starts the
 * application again to prove that the data was saved.
 *
 * The full screen output of each run is written to the "test-output" folder.
 */
public final class EndToEndTests {

    /** Every run pretends that today is Tuesday 17 November 2026, 10:00 in India. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-17T04:30:00Z"), ZoneId.of("Asia/Kolkata"));

    private EndToEndTests() {
    }

    /** Runs the application with the given typed lines and returns everything it printed. */
    static String runApp(Path dataFolder, List<String> typedLines) throws UnsupportedEncodingException {
        String typed = String.join(System.lineSeparator(), typedLines) + System.lineSeparator();
        ByteArrayOutputStream screen = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(screen, true, "UTF-8");
        NutriPlannerApp app = new NutriPlannerApp(new ByteArrayInputStream(typed.getBytes(StandardCharsets.UTF_8)),
                out, dataFolder, CLOCK, false);
        app.setEchoInput(true);
        app.run();
        return screen.toString("UTF-8");
    }

    private static void saveTranscript(String name, String text) throws IOException {
        Path folder = Paths.get("test-output");
        Files.createDirectories(folder);
        Files.write(folder.resolve(name), text.getBytes(StandardCharsets.UTF_8));
    }

    public static void run(TestSupport t) throws Exception {
        Path folder = TestSupport.tempFolder();
        try {
            String[] firstRun = {""};

            t.test("Complete first session: register, every menu option, logout and exit", () -> {
                List<String> typed = Arrays.asList(
                        // Welcome screen: invalid choices, then a failed login
                        "abc", "7",
                        "2", "ghost", "nothing1", "",
                        // Registration with many invalid answers
                        "1",
                        "pr", "priya sharma", "priya",
                        "abc", "Priya123", "Priya124", "Priya123", "Priya123",
                        "", "Priya Sharma",
                        "ten", "12", "21",
                        "5", "2",
                        "-160", "abc", "160",
                        "0", "55",
                        "2", "1", "1",
                        "",
                        // Trying to register the same username again, then cancelling
                        "1", "PRIYA", "",
                        // Login with a wrong password first
                        "2", "priya", "wrong12", "priya", "Priya123",
                        // Main menu: invalid choice, then View Profile
                        "10", "1", "",
                        // Update Profile: weight (with a mistake), goal (kept), back
                        "2", "5", "abc", "54", "8", "", "11",
                        // BMI: my BMI, another height and weight, back
                        "3", "1", "", "2", "170", "65", "", "3",
                        // Daily calorie requirements
                        "4", "",
                        // Food database: search, too-short search, details, my diet, add a food, by category, back
                        "5",
                        "2", "dosa", "",
                        "2", "x",
                        "5", "1", "",
                        "4", "",
                        "6", "Ragi dosa", "1", "1 piece (70 g)", "110", "3", "20", "2", "3", "y",
                        "3", "5", "",
                        "7",
                        // Meal planner
                        "6",
                        "1", "1", "idli", "0", "2", "y", "sambar", "1", "n",
                        "1", "2", "14", "2", "y", "dal", "1", "1.5", "y", "chicken", "2", "n", "n",
                        "1", "3", "tea", "1", "n",
                        "1", "4", "ragi", "2", "n",
                        "2", "",
                        "3", "1", "1", "3",
                        "4", "1", "2", "y",
                        "5", "3", "n",
                        "6", "31-02-2026", "tomorrow",
                        "1", "1", "banana", "1", "n",
                        "6", "today",
                        "7",
                        // Daily nutrition tracker: today, then tomorrow, back
                        "7", "1", "tomorrow", "2",
                        // Reports: today's report (saved), 7-day summary (not saved), back
                        "8", "1", "y", "3", "n", "4",
                        // Logout and exit
                        "9", "3");
                String screen = runApp(folder, typed);
                firstRun[0] = screen;
                saveTranscript("session1_first_run.txt", screen);

                String[] expected = {
                    "YOUR PERSONAL NUTRITION PLANNER",
                    "Invalid choice 'abc'. Please enter a number from 1 to 3.",
                    "Invalid username or password.",
                    "Username must be between 3 and 20 characters long.",
                    "Username can contain only letters, numbers and underscores",
                    "Password must be between 6 and 64 characters long.",
                    "The two passwords do not match.",
                    "Name cannot be empty.",
                    "'ten' is not a whole number.",
                    "Please enter a number from 15 to 100.",
                    "The value cannot be negative.",
                    "'abc' is not a number.",
                    "Please enter a value from 25 to 300.",
                    "Registration successful. Welcome to NutriPlanner, Priya Sharma!",
                    "The username 'priya' is already taken.",
                    "You have 2 attempts left.",
                    "Login successful. Welcome back, Priya Sharma!",
                    "Invalid choice '10'. Please enter a number from 1 to 9.",
                    "Daily calorie target  : 1,266 kcal",
                    "Profile saved. Your daily calorie target is now 1,252 kcal.",
                    "Calculation           : 54.0 / (1.6 x 1.6) = 21.1",
                    "Calculation           : 65.0 / (1.7 x 1.7) = 22.5",
                    "BMR = 10 x weight + 6.25 x height - 5 x age - 161",
                    "= 1,274 kcal per day",
                    "Daily calorie target  : 1,252 kcal",
                    "Masala dosa",
                    "Please type at least 2 letters to search.",
                    "NUTRITIONAL INFORMATION",
                    "Food added with ID 1001.",
                    "Please enter a value from 0.25 to 20.",
                    "Added 2 x Idli to Breakfast (116 kcal).",
                    "Added 1.5 x Dal (toor dal) to Lunch (240 kcal).",
                    "Chicken curry is non-vegetarian, which does not match your vegetarian",
                    "Added 2 x Ragi dosa to Dinner (220 kcal).",
                    "Idli is now 3 serving(s), 174 kcal.",
                    "Sambar was removed from Breakfast.",
                    "'31-02-2026' is not a valid date.",
                    "Now planning meals for Wed, 18 Nov 2026.",
                    "Added 1 x Banana to Breakfast (105 kcal).",
                    "Calories consumed     : 944 kcal",
                    "Remaining calories    : 308 kcal",
                    "Calories consumed     : 105 kcal",
                    "NUTRIPLANNER - DAILY NUTRITION REPORT",
                    "Report saved as a text file:",
                    "NUTRIPLANNER - 7-DAY NUTRITION SUMMARY",
                    "You have been logged out.",
                    "Thank you for using NutriPlanner. Your data has been saved. Goodbye!"
                };
                for (String text : expected) {
                    assertContains(screen, text, "first session output");
                }
                assertNotContains(screen, "Something unexpected went wrong", "an unexpected error happened");
                assertNotContains(screen, "Exception", "a stack trace or exception name was printed");
                check(Files.exists(folder.resolve("reports").resolve("priya_2026-11-17_daily_report.txt")),
                        "the saved report file exists");
            });

            t.test("Data persistence after restarting the application", () -> {
                List<String> typed = Arrays.asList(
                        "2", "priya", "Priya123",
                        "1", "",            // profile shows the updated weight
                        "7", "2",           // tracker for today
                        "6", "2", "", "7",  // view today's meals
                        "5", "2", "ragi", "", "7",  // the custom food is still there
                        "9", "3");
                String screen = runApp(folder, typed);
                saveTranscript("session2_after_restart.txt", screen);
                assertContains(screen, "Login successful. Welcome back, Priya Sharma!", "login after restart");
                assertContains(screen, "Weight                : 54.0 kg", "updated weight kept");
                assertContains(screen, "Calories consumed     : 944 kcal", "meals kept");
                assertContains(screen, "Ragi dosa", "custom food kept");
                assertContains(screen, "DAY TOTAL: 944 kcal", "meal details kept");
                assertNotContains(screen, "Something unexpected went wrong", "unexpected error");
            });

            t.test("Logout and exit, and the program closes cleanly when input ends", () -> {
                String screen = runApp(folder, Arrays.asList("2", "priya"));
                assertContains(screen, "Input ended, so NutriPlanner has closed.", "clean close");
                String exit = runApp(folder, Arrays.asList("3"));
                assertContains(exit, "Goodbye!", "exit from the welcome screen");
            });

            t.test("Three wrong passwords return to the welcome screen", () -> {
                String screen = runApp(folder, Arrays.asList("2", "priya", "bad1", "priya", "bad2", "priya", "bad3", "3"));
                assertContains(screen, "You have 1 attempt left.", "attempt counter");
                assertContains(screen, "Too many failed attempts. Returning to the welcome screen.", "lock out");
                assertNotContains(screen, "MAIN MENU", "never logged in");
            });

            t.test("Damaged data file lines are reported at start-up", () -> {
                Path users = folder.resolve("users.csv");
                Files.write(users, Arrays.asList("this line is damaged"), StandardCharsets.UTF_8,
                        java.nio.file.StandardOpenOption.APPEND);
                String screen = runApp(folder, Arrays.asList("2", "priya", "Priya123", "9", "3"));
                assertContains(screen, "Note: users.csv line", "warning shown");
                assertContains(screen, "Login successful", "good data still loaded");
            });

            t.test("Every screen line fits in a normal terminal window", () -> {
                for (String line : firstRun[0].split("\\R")) {
                    // Echoed answers come after prompts, so allow a little extra room.
                    check(line.length() <= 100, "line too wide (" + line.length() + "): " + line);
                }
            });
        } finally {
            TestSupport.deleteFolder(folder);
        }
    }
}
