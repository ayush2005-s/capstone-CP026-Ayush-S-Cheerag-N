# NutriPlanner

**Your personal nutrition planner: a terminal application written entirely in Java.**

NutriPlanner helps students and other users understand their food habits. It lets them:

- calculate BMI and daily calorie needs
- look up the nutrition of common Indian foods
- plan breakfast, lunch, evening snacks and dinner
- track the day's calories, protein, carbohydrates and fat against personal targets
- produce reports that can be saved as text files

It meets the project rules:

- **Java only.** Core Java and object-oriented programming. No other programming language.
- **Terminal only.** Every screen uses `System.out.println`, and every answer is read with `Scanner`.
- **No GUI, website, Swing, JavaFX, Spring or other framework.**
- **No external libraries.** It needs only the JDK: no Maven, no JDBC driver, no jar files.
- **Data is kept** in simple CSV text files, so it survives restarts.

---

## 1. Features

| Module | What it does |
|---|---|
| **A. Registration and login** | Register with a username and password, log in and log out. Usernames are validated and must be unique. Passwords are stored only as salted PBKDF2 hashes. After 3 wrong passwords you return to the welcome screen. In a real terminal, passwords are hidden while you type. |
| **B. User profile** | Name, age, gender, height, weight, activity level, dietary preference (Vegetarian / Non-Vegetarian / Vegan) and health goal (Weight Loss / Weight Maintenance / Weight Gain). You can view the profile and update one field, all fields, or your password. |
| **C. BMI calculator** | BMI = weight (kg) / height (m)². Shows the category (WHO), what it means and the healthy weight range for your height. Works for your profile or for any height and weight. |
| **D. Daily calorie calculator** | Works out BMR (Mifflin-St Jeor), then TDEE (BMR × activity multiplier), then the target for your goal (−500 / 0 / +500 kcal). The weight-loss target never drops below a safe minimum. Also gives protein, carbohydrate and fat targets. The working is shown step by step, with a clear "estimate, not a prescription" notice. |
| **E. Food database** | 74 built-in foods with clearly marked sample values per serving: idli, dosa, chapati, roti, rice, dal, sambar, paneer, curd, milk, upma, poha, fruits, vegetables and more. You can view all foods, search, filter by category or by your diet, view full nutrition details and add your own foods (saved permanently). |
| **F. Meal planning** | Choose a meal and a date, search for a food and enter the number of servings to add it. You can also change servings, remove a food, clear a meal and see the totals for each meal and for the whole day. You are warned if a food does not match your diet. |
| **G. Daily nutrition tracking** | Shows your daily calorie target, calories eaten and remaining calories, plus protein, carbohydrate and fat intake against your targets, with progress bars and practical advice. |
| **H. Reports** | A full daily report (profile, BMI, calorie needs, meal details, total intake, remaining allowance) and a 7-day summary. Either report can be saved as a `.txt` file. |
| **I. Data persistence** | Users, custom foods and meals are saved after every change and loaded when the program starts. A missing file just means there is no data yet. A damaged line is skipped with a warning, and the program does not crash. |

---

## 2. Requirements

- **JDK 11 or newer.** JDK 17 or 21 is recommended. Check your version by typing `java -version` and `javac -version` in a terminal.
- Nothing else. The project uses only the standard Java library.

---

## 3. Project structure

```
NutriPlanner/
├── README.md                  this file
├── docs/
│   ├── CLASS_GUIDE.md         what every class does, and the OOP concepts used
│   ├── TESTING.md             test cases and results
│   └── SAMPLE_SESSION.txt     every screen of a complete session
├── src/com/nutriplanner/
│   ├── Main.java              entry point
│   ├── NutriPlannerApp.java   creates the objects, loads data, starts the menus
│   ├── exception/             custom exceptions (5 classes)
│   ├── model/                 data classes and enums (User, Food, Meal, ...)
│   ├── service/               logic (calculators, food database, meal planner, ...)
│   ├── storage/               saving and loading CSV files
│   ├── ui/                    terminal menus and input handling
│   └── util/                  number formatting and text tables
├── test/com/nutriplanner/     49 automated tests (plain Java, no JUnit needed)
└── .vscode/                   ready-made run settings for VS Code
```

When you run the program, it creates a `data` folder next to `src`:

```
data/
├── users.csv           accounts and profiles (passwords as hashes only)
├── custom_foods.csv    foods added by users
├── meal_plans.csv      every food added to every meal
└── reports/            reports you chose to save
```

---

## 4. How to compile and run

Open a terminal **in the NutriPlanner folder** (the folder that contains `src`). The same two commands work on Windows (Command Prompt or PowerShell), macOS and Linux.

**Compile:**

```
javac -d out -sourcepath src src/com/nutriplanner/Main.java
```

**Run:**

```
java -cp out com.nutriplanner.Main
```

`javac` starts from `Main.java` and automatically compiles every class it uses, writing them into the `out` folder.

### Run the automated tests

```
javac -d out -sourcepath src src/com/nutriplanner/Main.java
javac -d out -cp out -sourcepath test test/com/nutriplanner/AllTests.java
java -cp out com.nutriplanner.AllTests
```

Expected ending:

```
Tests run: 49, passed: 49, failed: 0
All tests passed.
```

The tests use temporary folders, so they never touch your real `data` folder. They also write the complete screen output of two scripted sessions to `test-output/`, so you can read exactly what the program printed.

---

## 5. Running in VS Code

1. Install a JDK (for example Eclipse Temurin 17 or 21) and the VS Code extension **Extension Pack for Java** (by Microsoft).
2. In VS Code choose **File → Open Folder…** and open the `NutriPlanner` folder.
3. Open the **Run and Debug** panel (Ctrl+Shift+D), choose **Run NutriPlanner** and press the green ▶ button.
   - The program runs in the **integrated terminal**, so you can type your answers there.
   - **Run all tests** runs the test suite the same way.
4. You can also open a terminal in VS Code (Ctrl+`) and use the commands in section 4.

The `.vscode` folder already tells VS Code where the source files are (`src` and `test`) and to run from the project folder, so the `data` folder is created in the right place.

---

## 6. Demo walkthrough (about 5 minutes)

A suggested order for presenting the project:

1. **Register:** choose `1`, then enter username `priya` and password `Priya123`, then the profile: Priya Sharma, 21, Female, 160 cm, 55 kg, Lightly active, Vegetarian, Weight Loss.
   - Show validation by typing `-160` as the height or `abc` as the age.
2. **Login:** choose `2`. Try a wrong password once to show the error, then log in properly.
3. **View Profile (1):** shows the details plus BMI and daily target.
4. **Calculate BMI (3 → 1):** 21.5, Normal weight, with the healthy weight range.
5. **Daily calorie requirements (4):** the working is shown step by step:
   - BMR = 1,284 kcal
   - TDEE = 1,766 kcal
   - target = 1,266 kcal
6. **Food Database (5):**
   - Search `dal`.
   - View details of food `1` (Idli).
   - Add a new food, for example *Ragi dosa*.
7. **Meal Planner (6):**
   - Add 2 idli and 1 sambar to Breakfast.
   - Add chapati and dal to Lunch.
   - Choose **View meals and nutritional totals**.
8. **Daily Nutrition Tracker (7):** shows the target, calories eaten and remaining calories, and the macros with progress bars.
9. **View Nutrition Report (8 → 1):** shows the full report; answer `y` to save it as a text file.
10. **Logout (9)**, then **Exit (3)**. Start the program again and log in: all the data is still there.

To start again with no data, close the program and delete the `data` folder.

---

## 7. Calculations used

| Calculation | Formula |
|---|---|
| BMI | weight (kg) ÷ height (m)² |
| BMI categories (WHO, adults) | below 18.5 Underweight · 18.5–24.9 Normal · 25.0–29.9 Overweight · 30.0+ Obese |
| BMR (Mifflin-St Jeor) | 10 × weight + 6.25 × height − 5 × age + s, where s = +5 (men), −161 (women), −78 (other: the average) |
| TDEE | BMR × 1.2 / 1.375 / 1.55 / 1.725 / 1.9 (sedentary to extra active) |
| Calorie target | TDEE − 500 (weight loss), TDEE (maintenance), TDEE + 500 (weight gain) |
| Safe minimum (weight loss) | 1,200 kcal (women/other) or 1,500 kcal (men), but never above TDEE |
| Macro targets | Weight loss 30/40/30 %, maintenance 20/50/30 %, gain 25/50/25 % (protein/carbs/fat); 4 kcal per g of protein or carbohydrate, 9 kcal per g of fat |

All results are **estimates**, not medical advice. The program says so on every calorie screen and in every report.

**Food values** are approximate sample values per serving for typical home-style cooking. They are based on commonly published figures (Indian Food Composition Tables 2017 and USDA FoodData Central) and are labelled as sample data in the program.

---

## 8. Input validation and error handling

The program never crashes on ordinary typing mistakes. Every question repeats until the answer is valid and explains what was wrong:

| Situation | Example message |
|---|---|
| Invalid menu choice | `Error: Invalid choice '12'. Please enter a number from 1 to 9.` |
| Empty input | `Error: This field cannot be empty.` / `Error: Name cannot be empty.` |
| Text instead of a number | `Error: 'abc' is not a number. Please use digits, for example 100.` |
| Negative height or weight | `Error: The value cannot be negative.` |
| Age out of range | `Error: Please enter a number from 15 to 100.` |
| Invalid food quantity | `Error: Please enter a value from 0.25 to 20.` |
| Wrong login | `Error: Invalid username or password.` plus `You have 2 attempts left.` |
| Username taken / weak password | `Error: The username 'priya' is already taken.` / `Error: Password must contain at least one letter and one number.` |
| Invalid date | `Error: '31-02-2026' is not a valid date. Please use DD-MM-YYYY, for example 17-11-2026.` |
| Missing data files | Not an error: the program starts with no data and creates the files when needed. |
| Damaged line in a data file | `Note: users.csv line 4 was skipped because it could not be read (...)`. All other data still loads. |
| Saving fails (for example, read-only folder) | `Error: Your change could not be saved. ...` The change is undone in memory, so the screen and the file stay in step. |
| Input ends (Ctrl+D, or Ctrl+Z then Enter on Windows) | The program closes cleanly with a message. |
| Any other unexpected error | Caught by the menu, which reports it and keeps running. `Main` has a final safety net. |

---

## 9. Security

- Passwords are never saved. Only a **salted PBKDF2-HMAC-SHA256 hash** is stored, with 100,000 rounds and a random 16-byte salt per user. This uses only the standard Java library (`javax.crypto`).
- Login shows the same message for an unknown user and a wrong password, so it does not reveal which usernames exist.
- In a real terminal, passwords are read with `java.io.Console` and are not shown on screen. In consoles that do not support this (some IDE output panels), the program falls back to `Scanner`.

---

## 10. Documentation

- **[docs/CLASS_GUIDE.md](docs/CLASS_GUIDE.md):** the architecture, an explanation of every class, how the OOP concepts are applied (with the exact classes), and how a request flows through the code.
- **[docs/TESTING.md](docs/TESTING.md):** how to run the tests, every test case mapped to the required workflows, and the latest results.
- **[docs/SAMPLE_SESSION.txt](docs/SAMPLE_SESSION.txt):** a complete recorded session showing every screen with typed answers, including many invalid ones.

---

## 11. Troubleshooting

| Problem | Fix |
|---|---|
| `'javac' is not recognized` / `command not found` | Install a JDK (not just a JRE) and add its `bin` folder to your PATH. Then open a new terminal. |
| `Could not find or load main class com.nutriplanner.Main` | Run the commands from the NutriPlanner folder, and compile first. |
| `release version ... not supported` or `class file version` errors | Your JDK is older than 11. Install JDK 17 or 21. |
| The program cannot read what I type in VS Code | Use the **Run NutriPlanner** configuration, which runs in the integrated terminal, not the Debug Console. |
| The `data` folder appears somewhere unexpected | It is created in the folder you run the program from. The first line the program prints shows its exact location. |
| I want to start fresh | Close NutriPlanner and delete the `data` folder. |

---

*NutriPlanner is an academic project. Its numbers are estimates for learning purposes and are not medical or dietary advice.*
