# NutriPlanner: class guide and OOP concepts

This guide explains how the code is organised, what every class does and where each object-oriented programming concept is used. It is written to help you explain the project in a demonstration or viva.

---

## 1. Architecture

The code is split into **layers**. Each layer only talks to the layer below it:

```
 +--------------------------------------------------------------+
 |  ui         Menus and screens: print text, read answers       |  Menu, MainMenu, MealPlannerMenu ...
 +--------------------------------------------------------------+
 |  service    Rules and calculations                            |  BMICalculator, MealPlanner, UserManager ...
 +--------------------------------------------------------------+
 |  storage    Saving and loading CSV files                      |  DataStorage, CsvUtil
 +--------------------------------------------------------------+
 |  model      The data itself (objects and enums)               |  User, Food, Meal, MealEntry ...
 +--------------------------------------------------------------+
   exception  Custom exceptions used by all layers
   util       Formatting helpers used by all layers
```

**Why layers?**

- The calculators do not know about the terminal, so they can be tested on their own.
- The menus do not know about files, so the storage format could change without touching any screen.

**Packages** (61 classes in total):

- `com.nutriplanner`: `Main` and `NutriPlannerApp`
- `com.nutriplanner.exception`: custom exceptions
- `com.nutriplanner.model`: data classes and enums
- `com.nutriplanner.service`: business logic
- `com.nutriplanner.storage`: file handling
- `com.nutriplanner.ui`: terminal user interface
- `com.nutriplanner.util`: formatting

---

## 2. Every class explained

### Start-up

| Class | Purpose |
|---|---|
| `Main` | The entry point (`public static void main`). It creates `NutriPlannerApp` with `System.in`, `System.out`, the `data` folder and the system clock, then runs it. A final `try/catch` makes sure an unexpected error prints a friendly message instead of a stack trace. |
| `NutriPlannerApp` | Builds every object in the right order: storage → user manager → food database → meal planner → authentication → tracker → report. It wraps them in an `AppContext`, prints any data-file warnings and starts the `WelcomeMenu`. It also catches `InputClosedException` (input ended) and `DataStorageException` (data folder unusable). |

### exception package

| Class | Purpose |
|---|---|
| `NutriPlannerException` | The base class of all custom exceptions. It extends `RuntimeException`. |
| `ValidationException` | A value broke a rule: negative weight, age out of range, duplicate username, invalid servings and so on. |
| `AuthenticationException` | Login failed. |
| `DataStorageException` | A file could not be read or written. It wraps the original `IOException`. |
| `InputClosedException` | No more input (Ctrl+D or Ctrl+Z). Lets the program close cleanly instead of looping forever. |

### model package (the data)

| Class | Purpose |
|---|---|
| `NutritionalValue` (interface) | Declares `getCalories()`, `getProtein()`, `getCarbohydrates()` and `getFat()`. It is implemented by `Food`, `MealEntry`, `Meal`, `DailyMealPlan`, `NutritionTotals` and `NutritionTargets`. |
| `NutritionTotals` | An immutable sum of nutrients. `NutritionTotals.of(list)` adds up any list of `NutritionalValue` objects. |
| `Food` | One food: ID, name, category, serving size, calories, protein, carbohydrates, fat and diet type, all per serving. It is immutable and validated in the constructor. It also contains the name-search logic. |
| `CustomFood` | **Extends `Food`.** A food added by a user. It also stores who added it, and overrides `isBuiltIn()` and `getSourceLabel()`. |
| `MealEntry` | One food in a meal plus the number of servings. It copies the food's values when created, so old records stay correct. Its nutrients are the per-serving values × servings. |
| `Meal` | One meal (breakfast, lunch, evening snacks or dinner) holding a `List<MealEntry>`. Its totals are the sum of its entries. |
| `DailyMealPlan` | All four meals of one user on one date, stored in an `EnumMap<MealType, Meal>`. |
| `User` | An account: username, password hash, creation date and a `UserProfile`. |
| `UserProfile` | Name, age, gender, height, weight, activity, diet and goal. Every setter validates its value. |
| `BmiResult` | The result of a BMI calculation: the BMI, its category and the healthy weight range. |
| `NutritionTargets` | BMR, TDEE, calorie target and macro targets. Implements `NutritionalValue`, so it can be compared with what was eaten. |
| `DailySummary` | Targets against what was eaten on one day, plus the remaining amounts. |
| `Labeled` (interface) | `getLabel()` and `getDescription()`. All the enums implement it, so one method can print any of them as a menu. |
| `Gender` (enum) | Male, Female, Other. Each holds its Mifflin-St Jeor constant (+5 / −161 / −78). |
| `ActivityLevel` (enum) | Five levels, each with its TDEE multiplier (1.2 to 1.9). |
| `DietType` (enum) | Vegetarian, Non-Vegetarian, Vegan. Its `allows(foodType)` method decides whether a food suits a diet. |
| `HealthGoal` (enum) | Weight Loss, Maintenance, Gain. Each holds its calorie adjustment and macro percentages. |
| `BmiCategory` (enum) | The WHO categories with their ranges and explanations. `BmiCategory.of(bmi)` finds the right one. |
| `MealType` (enum) | Breakfast, Lunch, Evening Snacks, Dinner. |
| `FoodCategory` (enum) | Food groups used to organise the database. |

### service package (the logic)

| Class | Purpose |
|---|---|
| `BMICalculator` | `calculateBmi(weight, height)` and `evaluate(...)`. The BMI is rounded to one decimal **before** the category is chosen, so the category always matches the number shown. |
| `CalorieCalculator` | `calculateBmr(...)` (Mifflin-St Jeor), `calculateTdee(...)` and the safe minimum calories. |
| `NutritionCalculator` | Combines the above with the user's goal to produce `NutritionTargets` (calorie target and macro grams). It also has helpers for the energy split of a food and a calorie check for new foods. |
| `PasswordHasher` | Hashes passwords with PBKDF2-HMAC-SHA256, a random salt and 100,000 iterations, and verifies them with a constant-time comparison. |
| `UserManager` | Keeps all users in a `LinkedHashMap`. It registers users (checking that the username is valid and unique and the password is strong), finds users, changes passwords and saves through `DataStorage`. |
| `Authentication` | `login(...)`, `logout()` and the current user. It gives one message for every failed login. |
| `SampleFoods` | The 74 built-in foods with their sample nutrition values. |
| `FoodDatabase` | Built-in foods plus custom foods in a `TreeMap` sorted by ID. It provides search (word-prefix, then category), filters by category or diet, find by ID or name, and adding custom foods with IDs from 1001. |
| `MealPlanner` | Stores every user's meals as `username → (date → DailyMealPlan)`. It adds, updates, removes and clears foods, saving after each change and **undoing the change if saving fails**. |
| `NutritionTracker` | Builds a `DailySummary` and produces simple advice: calories left, over target, low protein, high carbohydrates or high fat. |
| `NutritionReport` | Builds the text of the daily report and the 7-day summary. It also provides the shared meal tables and the target-versus-eaten table that other screens use, so nothing is duplicated. |

### storage package (file handling)

| Class | Purpose |
|---|---|
| `CsvUtil` | Turns a list of values into one CSV line and back. Handles commas and quotes inside values. |
| `DataStorage` | Loads and saves `users.csv`, `custom_foods.csv` and `meal_plans.csv`, and saves report files. A missing file means "no data yet". Damaged lines are skipped and reported. Files are written to a `.tmp` file first and then moved into place, so a crash cannot leave a half-written file. |

### ui package (the terminal interface)

| Class | Purpose |
|---|---|
| `Menu` (abstract) | The shared menu loop: print the title and numbered options, read a valid choice, call `handleChoice(choice)` and catch errors. Subclasses only fill in the title, options and actions. |
| `WelcomeMenu` | Register / Login / Exit, with the NUTRIPLANNER banner. |
| `MainMenu` | The 9 main options after login. |
| `UpdateProfileMenu` | Change one profile field, all fields or the password. Saves after each change and shows the new calorie target. |
| `BmiMenu` | BMI for your profile or for any height and weight, with explanation and categories. |
| `FoodDatabaseMenu` | View all foods, search, view by category, view by your diet, view a food's details, add a food. |
| `MealPlannerMenu` | Add food, view meals and totals, change servings, remove, clear a meal, change the date. |
| `TrackerMenu` | The daily nutrition tracker for today or any date. |
| `ReportMenu` | Daily report, report for another date, 7-day summary, and saving to a file. |
| `RegistrationScreen` | Asks for a username, password (twice) and profile, then registers the user. |
| `LoginScreen` | Up to 3 login attempts. |
| `ProfileScreen` | Shows the profile, BMI and daily target. |
| `CalorieScreen` | Shows the BMR → TDEE → target calculation step by step, plus the macros. |
| `ProfileForm` | Asks for profile fields. It is reused by registration and by profile updates (where pressing Enter keeps the current value). |
| `FoodPicker` | Finds a food by ID or by name, and shows a numbered choice when several foods match. |
| `FoodTable` | Prints lists of foods as aligned tables. |
| `ConsoleInput` | All keyboard input via `Scanner`: whole numbers, decimals, yes/no, menu choices, enum choices, dates and hidden passwords. Every method repeats until the answer is valid. |
| `ConsoleView` | All screen output: banner, headers, sections, `label : value` fields, and wrapped notes and errors. |
| `AppContext` | Holds the input, output and all services, so every screen shares the same data. Also provides today's date from a `Clock`. |

### util package

| Class | Purpose |
|---|---|
| `Fmt` | Formats numbers (`1,266`), dates (`Tue, 17 Nov 2026`), percentages, progress bars, padding and word wrapping. Always uses `Locale.US`, so decimals use a dot on every computer. |
| `TextTable` | Builds aligned text tables with left- or right-aligned columns and separator lines. |

---

## 3. OOP concepts and where they are used

### Encapsulation

All fields are `private`. Data is only read through getters and only changed through methods that check the new value.

- `UserProfile.setWeightKg(-5)` throws `ValidationException`, so a profile can never hold an impossible value.
- `Food` has no setters at all: once created it cannot change, which makes it **immutable**.
- `Meal.getEntries()` returns an *unmodifiable* list, so other classes cannot add entries without going through `Meal.addEntry()`.

### Inheritance

- `CustomFood extends Food`: a custom food *is a* food, with one extra field (`addedBy`).
- `WelcomeMenu`, `MainMenu`, `BmiMenu`, `FoodDatabaseMenu`, `MealPlannerMenu`, `TrackerMenu`, `ReportMenu` and `UpdateProfileMenu` all `extend Menu`.
- `ValidationException`, `AuthenticationException`, `DataStorageException` and `InputClosedException` all `extend NutriPlannerException`, which extends `RuntimeException`.

### Polymorphism

- **Method overriding.**
  - Each menu overrides `getTitle()`, `getOptions()` and `handleChoice(int)`. The same `Menu.run()` code behaves differently depending on which menu object it runs on.
  - `CustomFood` overrides `getSourceLabel()`: the details screen calls `food.getSourceLabel()` without knowing which kind of food it has.
  - `WelcomeMenu` overrides `printHeader()` to show the big banner.
- **Interface polymorphism.** `NutritionTotals.of(Collection<? extends NutritionalValue>)` adds up foods, meal entries, meals or whole days with the same code. A `DailyMealPlan`'s totals are the sum of its `Meal`s, and each `Meal`'s total is the sum of its `MealEntry`s.
- **Generic method.** `ConsoleInput.chooseOption(title, E[] options, current)` works for every enum that implements `Labeled`, such as gender, activity, diet, goal, meal type and food category.

### Abstraction

- `Menu` is an **abstract class**. It defines *what* every menu does (show, read, handle) and leaves *how* to the subclasses. This is the *template method* design pattern.
- `NutritionalValue` and `Labeled` are **interfaces**. Code depends on what an object can do, not on its exact class.
- `DataStorage` hides the file format. The services call `saveMealPlans(...)` and never see CSV.

### Composition ("has a")

- A `User` *has a* `UserProfile`.
- A `DailyMealPlan` *has* four `Meal`s.
- A `Meal` *has* many `MealEntry` objects.

### Exception handling

- **Custom exceptions:** see the exception package.
- **Validation:** `ConsoleInput` catches `NumberFormatException` and `DateTimeParseException` and asks again.
- **The menu loop** in `Menu.run()` catches `ValidationException`, `AuthenticationException`, `DataStorageException` and any other `RuntimeException`, prints a message and keeps running.
- **File errors:** `DataStorage` converts `IOException` into `DataStorageException`. `MealPlanner`, `UserManager` and `FoodDatabase` undo their in-memory change when saving fails.
- **Clean exit:** `NutriPlannerApp` catches `InputClosedException` to close cleanly, and `Main` has a final safety net.

### Collections Framework

| Collection | Used for |
|---|---|
| `ArrayList` | Entries in a meal, search results, rows of a table |
| `LinkedHashMap` | Users by username (keeps registration order) |
| `TreeMap` | Foods by ID (sorted), and plans by date (sorted) |
| `HashMap` | Plans by user |
| `EnumMap` | Meals by `MealType`, and calories by meal |
| `HashSet` | Checking for duplicate IDs and names in the tests |
| `Collections.unmodifiableList` / `unmodifiableMap` | Protecting internal lists and maps |
| `Comparator` | Sorting search results |
| `Optional` | Finding users and foods that may not exist |

### File handling

- `java.nio.file.Files` with `BufferedReader` and `BufferedWriter` (UTF-8).
- `Files.createDirectories` creates the data folder.
- Each file is written to a temporary file first, then moved into place with `Files.move` (atomic).
- Reports are saved as `.txt` files in `data/reports/`.

### Modular programming

- 61 small classes in 6 packages, each with one job.
- `Main` is about 30 lines long.
- Shared pieces are written once and reused:
  - `ProfileForm` by registration and profile update
  - `FoodPicker` by the food database and the meal planner
  - `NutritionReport`'s meal and comparison tables by the meal planner, the tracker and the reports

---

## 4. Following one action through the code

**"Add 2 idli to breakfast":**

1. `MainMenu.handleChoice(6)` opens `MealPlannerMenu`.
2. `MealPlannerMenu.handleChoice(1)` calls `addFood()`. It asks for the meal with `ConsoleInput.chooseOption(...)`, which returns `MealType.BREAKFAST`.
3. `FoodPicker.pick()` reads "idli" and calls `FoodDatabase.search("idli")`, which finds one match: `Food` #1.
4. `DietType.allows(...)` checks that the food suits the user's diet.
5. `ConsoleInput.readDouble(...)` reads `2`, which must be between 0.25 and 20.
6. `MealPlanner.addFood(...)` builds a `MealEntry` with `MealEntry.fromFood(food, 2)`, adds it to the `Meal` and calls `DataStorage.saveMealPlans(...)`.
7. `DataStorage` writes `meal_plans.csv` (via a `.tmp` file).
8. The screen prints `Added 2 x Idli to Breakfast (116 kcal).`, which is the sum the `NutritionalValue` methods return.

**"Daily Nutrition Tracker":**

1. `TrackerMenu.printContext()` calls `NutritionTracker.summarize(profile, plan)`.
2. `NutritionCalculator.calculateTargets(profile)` gives BMR → TDEE → target and macros.
3. `NutritionTotals.of(plan.getMeals())` adds up what was eaten.
4. `NutritionReport.renderComparison(summary)` prints the target / eaten / remaining table, and `NutritionTracker.buildAdvice(...)` adds the advice lines.
