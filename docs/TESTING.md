# NutriPlanner: testing summary

## 1. How to run the tests

From the NutriPlanner folder:

```
javac -d out -sourcepath src src/com/nutriplanner/Main.java
javac -d out -cp out -sourcepath test test/com/nutriplanner/AllTests.java
java -cp out com.nutriplanner.AllTests
```

In VS Code, choose **Run all tests** in the Run and Debug panel.

- The tests are written in plain Java, in `test/com/nutriplanner/`, with a small home-made framework (`TestSupport`). No JUnit or other library is needed.
- Every test uses its own temporary folder, so your real `data` folder is never touched.
- The end-to-end tests run the **real application** with a script of typed answers and a fixed date (Tue, 17 Nov 2026). They save the complete screen output to:
  - `test-output/session1_first_run.txt`: registration, every menu option, around 25 invalid answers, logout and exit
  - `test-output/session2_after_restart.txt`: the program started again, showing that every piece of data was kept
- A copy of the first session is included as **`docs/SAMPLE_SESSION.txt`**. It shows every screen of the program, with typed answers, and is useful as a reference for a demonstration. Its file paths have been replaced with an example path.

| Test class | What it covers |
|---|---|
| `CalculatorTests` | BMI, BMI categories, BMR, TDEE, calorie targets, macros, profile validation |
| `FoodDatabaseTests` | Required foods, unique IDs, search, diet filter, custom foods |
| `UserTests` | Registration, login, logout, password hashing, password change, saving users |
| `MealPlannerTests` | Meal creation, meal-wise and daily totals, changes, invalid quantities, saving meals |
| `StorageAndReportTests` | CSV format, missing and damaged files, tracking, reports, saving reports |
| `EndToEndTests` | Complete scripted sessions through every menu, plus a restart |

## 2. Required workflows and the tests that cover them

| # | Workflow from the project brief | Test(s) | Result |
|---|---|---|---|
| 1 | New user registration | `New user registration`; end-to-end first session | PASS |
| 2 | Existing user login | `Existing user login works and logout ends the session`; end-to-end sessions 1 and 2 | PASS |
| 3 | Invalid login | `Invalid login: wrong password, unknown user and empty fields`; `Three wrong passwords return to the welcome screen` | PASS |
| 4 | Profile creation and updating | `Profile rejects impossible values`; `Users and profile changes are still there after a restart`; end-to-end (weight changed from 55 to 54 kg, new target 1,252 kcal) | PASS |
| 5 | BMI calculation | `BMI formula: 70 kg and 175 cm gives 22.9`; `BMI categories at the WHO boundaries`; `BMI category matches the rounded value shown to the user`; `Healthy weight range for 160 cm is 47.4 to 63.7 kg` | PASS |
| 6 | BMR and daily calorie calculation | `Mifflin-St Jeor BMR matches the published equation`; `TDEE uses the activity multipliers`; `Calorie target follows the health goal`; `Weight-loss target never goes below the safe minimum`; `Macronutrient grams follow the goal percentages` | PASS |
| 7 | Food search | `Search ignores case and lists names that start with the text first`; `Search matches the start of words only`; `Search falls back to categories, so 'fruit' lists fruits`; `Search needs at least two letters and handles no matches` | PASS |
| 8 | Meal creation | `Meal creation and meal nutritional calculations`; end-to-end (idli, sambar, chapati, dal, tea, ragi dosa, banana) | PASS |
| 9 | Meal nutritional calculations | `Meal creation and meal nutritional calculations` (2 idli + 1 sambar = 226 kcal, 9.0 g protein); `Daily total adds up all four meals` (706 kcal) | PASS |
| 10 | Daily nutrition tracking | `Daily nutrition tracking compares intake with targets` (845 kcal eaten, 421 left); `Over-target days are reported`; end-to-end (944 eaten, 308 left) | PASS |
| 11 | Report generation | `Report generation includes every required section`; `7-day summary and saving a report to a text file`; end-to-end (report shown and saved) | PASS |
| 12 | Data persistence after restarting | `Users and profile changes are still there after a restart`; `Custom food gets ID 1001, is saved and is found after a restart`; `Data persistence: meals are reloaded after a restart`; `Data persistence after restarting the application` | PASS |
| 13 | Logout and application exit | `Existing user login works and logout ends the session`; `Logout and exit, and the program closes cleanly when input ends`; end-to-end first session | PASS |

## 3. Input validation and error handling cases

All of these are typed into the real program by the end-to-end test, and the expected message is checked:

| Input | Where | Expected response |
|---|---|---|
| `abc`, `7` | Welcome menu | `Invalid choice 'abc'. Please enter a number from 1 to 3.` |
| `10` | Main menu | `Invalid choice '10'. Please enter a number from 1 to 9.` |
| `ghost` / `nothing1` | Login | `Invalid username or password.` + `You have 2 attempts left.` |
| 3 wrong passwords | Login | `Too many failed attempts. Returning to the welcome screen.` |
| `pr` / `priya sharma` | Username | length rule / letters, numbers and underscores only |
| `PRIYA` (already registered) | Username | `The username 'priya' is already taken.` |
| `abc` / mismatched confirmation | Password | length rule / `The two passwords do not match.` |
| (empty) | Full name | `Name cannot be empty.` |
| `ten` / `12` | Age | `'ten' is not a whole number.` / `Please enter a number from 15 to 100.` |
| `5` | Gender (1-3) | `Please enter a number from 1 to 3.` |
| `-160` / `abc` | Height | `The value cannot be negative.` / `'abc' is not a number.` |
| `0` | Weight | `Please enter a value from 25 to 300.` |
| `x` | Food search | `Please type at least 2 letters to search.` |
| `0` | Servings | `Please enter a value from 0.25 to 20.` |
| `chicken` → Chicken curry | Vegetarian user's lunch | `Note: Chicken curry is non-vegetarian, which does not match your vegetarian preference.` + `Add it anyway?` |
| `31-02-2026` | Date | `'31-02-2026' is not a valid date. Please use DD-MM-YYYY ...` |
| Damaged line added to `users.csv` | Start-up | `Note: users.csv line ... was skipped ...`, and login still works |
| Input ends in the middle of a menu | Anywhere | `Input ended, so NutriPlanner has closed. Your data has been saved.` |

The end-to-end test also checks that:

- the words "Exception" and "Something unexpected went wrong" never appear on screen
- every screen line fits a normal terminal window

## 4. Expected values used in the tests (worked by hand)

| Case | Working | Result |
|---|---|---|
| BMI, 70 kg, 175 cm | 70 / 1.75² = 22.86 | 22.9, Normal weight |
| BMR, man, 30 y, 175 cm, 70 kg | 10×70 + 6.25×175 − 5×30 + 5 | 1,648.75 kcal |
| BMR, woman, same values | 10×70 + 6.25×175 − 5×30 − 161 | 1,482.75 kcal |
| TDEE, moderately active man | 1,648.75 × 1.55 | 2,555.6 kcal |
| Targets for that man | loss / maintain / gain | 2,056 / 2,556 / 3,056 kcal |
| Priya: woman, 21 y, 160 cm, 55 kg, lightly active, loss | BMR 1,284 → TDEE 1,766 → −500 | **1,266 kcal**; protein 95 g, carbohydrates 127 g, fat 42 g |
| Sedentary man, 40 y, 165 cm, 60 kg, loss | TDEE 1,723.5 − 500 = 1,224, which is below 1,500 | raised to 1,500 kcal |
| Breakfast of 2 idli + 1 sambar | 2×58 + 110 | 226 kcal, 9.0 g protein |

## 5. Latest results

```
Calculators (BMI, BMR, TDEE, calorie targets)
  PASS  BMI formula: 70 kg and 175 cm gives 22.9
  PASS  BMI categories at the WHO boundaries
  PASS  BMI category matches the rounded value shown to the user
  PASS  Healthy weight range for 160 cm is 47.4 to 63.7 kg
  PASS  BMI rejects zero and negative values
  PASS  Mifflin-St Jeor BMR matches the published equation
  PASS  TDEE uses the activity multipliers
  PASS  Calorie target follows the health goal
  PASS  Weight-loss target never goes below the safe minimum
  PASS  Macronutrient grams follow the goal percentages
  PASS  Energy split of a food adds up to 100%
  PASS  Profile rejects impossible values
Food database
  PASS  Database contains the required Indian foods
  PASS  Food IDs and names are unique and values are sensible
  PASS  Search ignores case and lists names that start with the text first
  PASS  Search matches the start of words only
  PASS  Search falls back to categories, so 'fruit' lists fruits
  PASS  Search needs at least two letters and handles no matches
  PASS  Diet filter: vegans and vegetarians only see suitable foods
  PASS  Custom food gets ID 1001, is saved and is found after a restart
  PASS  Invalid or duplicate custom foods are rejected
Users, login and passwords
  PASS  New user registration
  PASS  Duplicate usernames are rejected, whatever the case
  PASS  Invalid usernames and weak passwords are rejected
  PASS  Existing user login works and logout ends the session
  PASS  Invalid login: wrong password, unknown user and empty fields
  PASS  Passwords are stored only as salted hashes
  PASS  Change password checks the current password
  PASS  Users and profile changes are still there after a restart
Meal planner
  PASS  Meal creation and meal nutritional calculations
  PASS  Daily total adds up all four meals
  PASS  Servings can be changed and foods removed
  PASS  Invalid food quantities are rejected
  PASS  Meals are kept separately for each user and date
  PASS  Data persistence: meals are reloaded after a restart
  PASS  Clearing a meal and listing a week of plans
Storage, tracker and reports
  PASS  CSV lines keep commas and quotes inside values
  PASS  Missing data files mean an empty start, not an error
  PASS  Damaged lines are skipped with a warning and good lines still load
  PASS  Daily nutrition tracking compares intake with targets
  PASS  Over-target days are reported
  PASS  Report generation includes every required section
  PASS  7-day summary and saving a report to a text file
Complete application runs (end to end)
  PASS  Complete first session: register, every menu option, logout and exit
  PASS  Data persistence after restarting the application
  PASS  Logout and exit, and the program closes cleanly when input ends
  PASS  Three wrong passwords return to the welcome screen
  PASS  Damaged data file lines are reported at start-up
  PASS  Every screen line fits in a normal terminal window

Tests run: 49, passed: 49, failed: 0
All tests passed.
```

The project was compiled with `javac --release 11 -Xlint:all` with no warnings, and the tests were run on JDK 21.

## 6. Manual checks done in a real terminal

- Password typing is hidden in a real terminal (`java.io.Console`). In consoles without that support it falls back to visible typing through `Scanner`.
- The source code uses only ASCII characters, so it compiles the same way on Windows, macOS and Linux whatever the system's default encoding.
- Screens are designed to fit within 76 characters, so they display without wrapping in a standard 80-column terminal window. Long file paths, such as the location of a saved report, are the only exception.
