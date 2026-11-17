package com.nutriplanner.storage;

import com.nutriplanner.exception.DataStorageException;
import com.nutriplanner.model.ActivityLevel;
import com.nutriplanner.model.CustomFood;
import com.nutriplanner.model.DailyMealPlan;
import com.nutriplanner.model.DietType;
import com.nutriplanner.model.FoodCategory;
import com.nutriplanner.model.Gender;
import com.nutriplanner.model.HealthGoal;
import com.nutriplanner.model.Meal;
import com.nutriplanner.model.MealEntry;
import com.nutriplanner.model.MealType;
import com.nutriplanner.model.User;
import com.nutriplanner.model.UserProfile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Saves and loads all NutriPlanner data as CSV text files in one folder.
 *
 * <ul>
 *   <li>users.csv        - accounts and profiles (passwords are stored only as salted hashes)</li>
 *   <li>custom_foods.csv - foods added by users</li>
 *   <li>meal_plans.csv   - every food added to every meal, one line per food</li>
 *   <li>reports/         - nutrition reports the user chose to save</li>
 * </ul>
 *
 * Files are written to a temporary file first and then moved into place, so a
 * crash in the middle of saving can never leave a half-written file behind.
 * Lines that cannot be read are skipped and reported as warnings instead of
 * stopping the program.
 */
public class DataStorage {

    public static final String USERS_FILE = "users.csv";
    public static final String CUSTOM_FOODS_FILE = "custom_foods.csv";
    public static final String MEAL_PLANS_FILE = "meal_plans.csv";
    public static final String REPORTS_FOLDER = "reports";

    private static final List<String> USER_COLUMNS = Arrays.asList("username", "password_hash", "name", "age",
            "gender", "height_cm", "weight_kg", "activity_level", "diet", "goal", "created_on");
    private static final List<String> FOOD_COLUMNS = Arrays.asList("id", "name", "category", "serving_size",
            "calories", "protein_g", "carbohydrates_g", "fat_g", "diet", "added_by");
    private static final List<String> MEAL_COLUMNS = Arrays.asList("username", "date", "meal", "food_id",
            "food_name", "serving_size", "calories", "protein_g", "carbohydrates_g", "fat_g", "servings");

    private final Path dataDirectory;
    private final List<String> warnings = new ArrayList<>();

    /**
     * Creates the storage and makes sure the data folder exists.
     *
     * @throws DataStorageException when the folder cannot be created
     */
    public DataStorage(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            throw new DataStorageException("Cannot create the data folder " + dataDirectory.toAbsolutePath(), e);
        }
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    /** Problems found while loading, for example a damaged line that was skipped. */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    // ------------------------------------------------------------------ users

    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        for (CsvRow row : readRows(USERS_FILE, USER_COLUMNS)) {
            try {
                List<String> v = row.values;
                UserProfile profile = new UserProfile(
                        v.get(2),
                        Integer.parseInt(v.get(3)),
                        Gender.valueOf(v.get(4)),
                        Double.parseDouble(v.get(5)),
                        Double.parseDouble(v.get(6)),
                        ActivityLevel.valueOf(v.get(7)),
                        DietType.valueOf(v.get(8)),
                        HealthGoal.valueOf(v.get(9)));
                users.add(new User(v.get(0), v.get(1), profile, LocalDate.parse(v.get(10))));
            } catch (RuntimeException e) {
                skip(USERS_FILE, row.lineNumber, e);
            }
        }
        return users;
    }

    public void saveUsers(Collection<User> users) {
        List<List<String>> rows = new ArrayList<>();
        for (User user : users) {
            UserProfile p = user.getProfile();
            rows.add(Arrays.asList(
                    user.getUsername(),
                    user.getPasswordHash(),
                    p.getName(),
                    String.valueOf(p.getAge()),
                    p.getGender().name(),
                    number(p.getHeightCm()),
                    number(p.getWeightKg()),
                    p.getActivityLevel().name(),
                    p.getDietType().name(),
                    p.getHealthGoal().name(),
                    user.getCreatedOn().toString()));
        }
        writeRows(USERS_FILE, USER_COLUMNS, rows);
    }

    // ------------------------------------------------------------ custom foods

    public List<CustomFood> loadCustomFoods() {
        List<CustomFood> foods = new ArrayList<>();
        for (CsvRow row : readRows(CUSTOM_FOODS_FILE, FOOD_COLUMNS)) {
            try {
                List<String> v = row.values;
                foods.add(new CustomFood(
                        Integer.parseInt(v.get(0)),
                        v.get(1),
                        FoodCategory.valueOf(v.get(2)),
                        v.get(3),
                        Double.parseDouble(v.get(4)),
                        Double.parseDouble(v.get(5)),
                        Double.parseDouble(v.get(6)),
                        Double.parseDouble(v.get(7)),
                        DietType.valueOf(v.get(8)),
                        v.get(9)));
            } catch (RuntimeException e) {
                skip(CUSTOM_FOODS_FILE, row.lineNumber, e);
            }
        }
        return foods;
    }

    public void saveCustomFoods(Collection<CustomFood> foods) {
        List<List<String>> rows = new ArrayList<>();
        for (CustomFood food : foods) {
            rows.add(Arrays.asList(
                    String.valueOf(food.getId()),
                    food.getName(),
                    food.getCategory().name(),
                    food.getServingSize(),
                    number(food.getCalories()),
                    number(food.getProtein()),
                    number(food.getCarbohydrates()),
                    number(food.getFat()),
                    food.getDietType().name(),
                    food.getAddedBy()));
        }
        writeRows(CUSTOM_FOODS_FILE, FOOD_COLUMNS, rows);
    }

    // -------------------------------------------------------------- meal plans

    public List<DailyMealPlan> loadMealPlans() {
        // Key: "username|date". A LinkedHashMap keeps the plans in file order.
        Map<String, DailyMealPlan> plans = new LinkedHashMap<>();
        for (CsvRow row : readRows(MEAL_PLANS_FILE, MEAL_COLUMNS)) {
            try {
                List<String> v = row.values;
                String username = v.get(0);
                LocalDate date = LocalDate.parse(v.get(1));
                MealType mealType = MealType.valueOf(v.get(2));
                MealEntry entry = new MealEntry(
                        Integer.parseInt(v.get(3)),
                        v.get(4),
                        v.get(5),
                        Double.parseDouble(v.get(6)),
                        Double.parseDouble(v.get(7)),
                        Double.parseDouble(v.get(8)),
                        Double.parseDouble(v.get(9)),
                        Double.parseDouble(v.get(10)));
                String key = username + "|" + date;
                DailyMealPlan plan = plans.get(key);
                if (plan == null) {
                    plan = new DailyMealPlan(username, date);
                    plans.put(key, plan);
                }
                plan.getMeal(mealType).addEntry(entry);
            } catch (RuntimeException e) {
                skip(MEAL_PLANS_FILE, row.lineNumber, e);
            }
        }
        return new ArrayList<>(plans.values());
    }

    public void saveMealPlans(Collection<DailyMealPlan> plans) {
        List<List<String>> rows = new ArrayList<>();
        for (DailyMealPlan plan : plans) {
            for (Meal meal : plan.getMeals()) {
                for (MealEntry e : meal.getEntries()) {
                    rows.add(Arrays.asList(
                            plan.getUsername(),
                            plan.getDate().toString(),
                            meal.getType().name(),
                            String.valueOf(e.getFoodId()),
                            e.getFoodName(),
                            e.getServingSize(),
                            number(e.getCaloriesPerServing()),
                            number(e.getProteinPerServing()),
                            number(e.getCarbohydratesPerServing()),
                            number(e.getFatPerServing()),
                            number(e.getServings())));
                }
            }
        }
        writeRows(MEAL_PLANS_FILE, MEAL_COLUMNS, rows);
    }

    // ----------------------------------------------------------------- reports

    /**
     * Saves a report as a text file in the reports folder.
     *
     * @return the path of the saved file
     */
    public Path saveReport(String fileName, String content) {
        Path folder = dataDirectory.resolve(REPORTS_FOLDER);
        Path file = folder.resolve(fileName);
        try {
            Files.createDirectories(folder);
            writeAtomically(file, content);
            return file;
        } catch (IOException e) {
            throw new DataStorageException("Cannot write the report file " + file.toAbsolutePath(), e);
        }
    }

    // ----------------------------------------------------------- file helpers

    /** One line of a CSV file, remembered with its line number for error messages. */
    private static final class CsvRow {
        final int lineNumber;
        final List<String> values;

        CsvRow(int lineNumber, List<String> values) {
            this.lineNumber = lineNumber;
            this.values = values;
        }
    }

    /**
     * Reads every data line of a CSV file. A missing file simply means there is
     * no data yet (for example on the first run), so an empty list is returned.
     */
    private List<CsvRow> readRows(String fileName, List<String> columns) {
        Path file = dataDirectory.resolve(fileName);
        List<CsvRow> rows = new ArrayList<>();
        if (!Files.exists(file)) {
            return rows;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty() || (lineNumber == 1 && line.startsWith(columns.get(0) + ","))) {
                    continue;   // skip blank lines and the header line
                }
                try {
                    List<String> values = CsvUtil.parseLine(line);
                    if (values.size() != columns.size()) {
                        warnings.add(fileName + " line " + lineNumber + " was skipped: expected "
                                + columns.size() + " values but found " + values.size() + ".");
                        continue;
                    }
                    rows.add(new CsvRow(lineNumber, values));
                } catch (IllegalArgumentException e) {
                    skip(fileName, lineNumber, e);
                }
            }
        } catch (IOException e) {
            throw new DataStorageException("Cannot read " + file.toAbsolutePath(), e);
        }
        return rows;
    }

    private void writeRows(String fileName, List<String> columns, List<List<String>> rows) {
        StringBuilder content = new StringBuilder();
        content.append(CsvUtil.toLine(columns)).append(System.lineSeparator());
        for (List<String> row : rows) {
            content.append(CsvUtil.toLine(row)).append(System.lineSeparator());
        }
        Path file = dataDirectory.resolve(fileName);
        try {
            writeAtomically(file, content.toString());
        } catch (IOException e) {
            throw new DataStorageException("Cannot save " + file.toAbsolutePath(), e);
        }
    }

    /** Writes to "name.tmp" first, then replaces the real file in one step. */
    private static void writeAtomically(Path file, String content) throws IOException {
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            writer.write(content);
        }
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void skip(String fileName, int lineNumber, Exception cause) {
        String reason = cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
        warnings.add(fileName + " line " + lineNumber + " was skipped because it could not be read (" + reason + ").");
    }

    /** Numbers are stored with a dot as the decimal mark on every computer. */
    private static String number(double value) {
        String text = String.format(Locale.US, "%.2f", value);
        // Remove unnecessary zeros: "65.00" -> "65", "1.50" -> "1.5".
        if (text.contains(".")) {
            text = text.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return text;
    }
}
