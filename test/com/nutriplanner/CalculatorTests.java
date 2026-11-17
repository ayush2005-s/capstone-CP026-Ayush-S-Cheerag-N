package com.nutriplanner;

import com.nutriplanner.exception.ValidationException;
import com.nutriplanner.model.ActivityLevel;
import com.nutriplanner.model.BmiCategory;
import com.nutriplanner.model.BmiResult;
import com.nutriplanner.model.DietType;
import com.nutriplanner.model.Gender;
import com.nutriplanner.model.HealthGoal;
import com.nutriplanner.model.NutritionTargets;
import com.nutriplanner.model.UserProfile;
import com.nutriplanner.service.BMICalculator;
import com.nutriplanner.service.CalorieCalculator;
import com.nutriplanner.service.NutritionCalculator;

import static com.nutriplanner.TestSupport.assertEquals;
import static com.nutriplanner.TestSupport.assertNear;
import static com.nutriplanner.TestSupport.assertThrows;
import static com.nutriplanner.TestSupport.check;

/** Tests for the BMI, BMR, TDEE and calorie target formulas. */
public final class CalculatorTests {

    private CalculatorTests() {
    }

    static UserProfile profile(Gender gender, int age, double height, double weight,
                               ActivityLevel activity, HealthGoal goal) {
        return new UserProfile("Test User", age, gender, height, weight, activity, DietType.VEGETARIAN, goal);
    }

    public static void run(TestSupport t) {
        t.test("BMI formula: 70 kg and 175 cm gives 22.9", () -> {
            assertNear(22.857, BMICalculator.calculateBmi(70, 175), 0.001, "exact BMI");
            BmiResult result = BMICalculator.evaluate(70, 175);
            assertNear(22.9, result.getBmi(), 1e-9, "rounded BMI");
            assertEquals(BmiCategory.NORMAL, result.getCategory(), "category");
        });

        t.test("BMI categories at the WHO boundaries", () -> {
            assertEquals(BmiCategory.UNDERWEIGHT, BmiCategory.of(18.4), "18.4");
            assertEquals(BmiCategory.NORMAL, BmiCategory.of(18.5), "18.5");
            assertEquals(BmiCategory.NORMAL, BmiCategory.of(24.9), "24.9");
            assertEquals(BmiCategory.OVERWEIGHT, BmiCategory.of(25.0), "25.0");
            assertEquals(BmiCategory.OVERWEIGHT, BmiCategory.of(29.9), "29.9");
            assertEquals(BmiCategory.OBESE, BmiCategory.of(30.0), "30.0");
            assertEquals(BmiCategory.OBESE, BmiCategory.of(55), "55");
        });

        t.test("BMI category matches the rounded value shown to the user", () -> {
            // 24.96 is shown as 25.0, so it must be reported as Overweight, not Normal.
            double weight = 24.96 * 1.7 * 1.7;
            BmiResult result = BMICalculator.evaluate(weight, 170);
            assertNear(25.0, result.getBmi(), 1e-9, "rounded BMI");
            assertEquals(BmiCategory.OVERWEIGHT, result.getCategory(), "category of rounded BMI");
        });

        t.test("Healthy weight range for 160 cm is 47.4 to 63.7 kg", () -> {
            BmiResult result = BMICalculator.evaluate(55, 160);
            assertNear(47.36, result.getHealthyMinWeightKg(), 0.01, "minimum");
            assertNear(63.74, result.getHealthyMaxWeightKg(), 0.01, "maximum");
        });

        t.test("BMI rejects zero and negative values", () -> {
            assertThrows(ValidationException.class, () -> BMICalculator.calculateBmi(-70, 175), "negative weight");
            assertThrows(ValidationException.class, () -> BMICalculator.calculateBmi(70, 0), "zero height");
        });

        t.test("Mifflin-St Jeor BMR matches the published equation", () -> {
            // 10 x 70 + 6.25 x 175 - 5 x 30 + 5 = 1648.75
            assertNear(1648.75, CalorieCalculator.calculateBmr(Gender.MALE, 70, 175, 30), 1e-9, "male");
            // 10 x 70 + 6.25 x 175 - 5 x 30 - 161 = 1482.75
            assertNear(1482.75, CalorieCalculator.calculateBmr(Gender.FEMALE, 70, 175, 30), 1e-9, "female");
            assertNear(1565.75, CalorieCalculator.calculateBmr(Gender.OTHER, 70, 175, 30), 1e-9, "other (average)");
        });

        t.test("TDEE uses the activity multipliers", () -> {
            assertNear(1978.5, CalorieCalculator.calculateTdee(1648.75, ActivityLevel.SEDENTARY), 1e-9, "sedentary");
            assertNear(2555.5625, CalorieCalculator.calculateTdee(1648.75, ActivityLevel.MODERATELY_ACTIVE), 1e-9,
                    "moderate");
            assertNear(3132.625, CalorieCalculator.calculateTdee(1648.75, ActivityLevel.EXTRA_ACTIVE), 1e-9, "extra");
        });

        t.test("Calorie target follows the health goal", () -> {
            UserProfile p = profile(Gender.MALE, 30, 175, 70, ActivityLevel.MODERATELY_ACTIVE, HealthGoal.WEIGHT_LOSS);
            assertEquals(2056, NutritionCalculator.calculateTargets(p).getCalorieTarget(), "loss: 2556 - 500");
            p.setHealthGoal(HealthGoal.WEIGHT_MAINTENANCE);
            assertEquals(2556, NutritionCalculator.calculateTargets(p).getCalorieTarget(), "maintenance = TDEE");
            p.setHealthGoal(HealthGoal.WEIGHT_GAIN);
            assertEquals(3056, NutritionCalculator.calculateTargets(p).getCalorieTarget(), "gain: 2556 + 500");
        });

        t.test("Weight-loss target never goes below the safe minimum", () -> {
            // BMR = 450 + 937.5 - 300 - 161 = 926.5, TDEE = 1111.8 (below 1200)
            UserProfile small = profile(Gender.FEMALE, 60, 150, 45, ActivityLevel.SEDENTARY, HealthGoal.WEIGHT_LOSS);
            NutritionTargets t1 = NutritionCalculator.calculateTargets(small);
            check(t1.isMinimumApplied(), "minimum should be applied");
            assertEquals(1112, t1.getCalorieTarget(), "never above TDEE");
            // 21-year-old woman: TDEE 1766, minus 500 = 1266, which is above 1200
            UserProfile priya = profile(Gender.FEMALE, 21, 160, 55, ActivityLevel.LIGHTLY_ACTIVE, HealthGoal.WEIGHT_LOSS);
            NutritionTargets t2 = NutritionCalculator.calculateTargets(priya);
            check(!t2.isMinimumApplied(), "no minimum needed");
            assertEquals(1266, t2.getCalorieTarget(), "1766 - 500");
            // Sedentary man: BMR 1436.25, TDEE 1723.5, minus 500 = 1224 -> raised to 1500
            UserProfile man = profile(Gender.MALE, 40, 165, 60, ActivityLevel.SEDENTARY, HealthGoal.WEIGHT_LOSS);
            NutritionTargets t3 = NutritionCalculator.calculateTargets(man);
            check(t3.isMinimumApplied(), "minimum for men");
            assertEquals(1500, t3.getCalorieTarget(), "men's floor");
        });

        t.test("Macronutrient grams follow the goal percentages", () -> {
            UserProfile p = profile(Gender.FEMALE, 21, 160, 55, ActivityLevel.LIGHTLY_ACTIVE, HealthGoal.WEIGHT_LOSS);
            NutritionTargets t1 = NutritionCalculator.calculateTargets(p);
            assertEquals(95, t1.getProteinGrams(), "30% of 1266 / 4");
            assertEquals(127, t1.getCarbohydrateGrams(), "40% of 1266 / 4");
            assertEquals(42, t1.getFatGrams(), "30% of 1266 / 9");
            double kcal = t1.getProteinGrams() * 4 + t1.getCarbohydrateGrams() * 4 + t1.getFatGrams() * 9;
            assertNear(1266, kcal, 15, "macros add up to the target");
        });

        t.test("Energy split of a food adds up to 100%", () -> {
            int[] split = NutritionCalculator.energySplit(2.0, 12.0, 0.2);
            assertEquals(100, split[0] + split[1] + split[2], "total");
            assertEquals(14, split[0], "protein share of idli");
        });

        t.test("Profile rejects impossible values", () -> {
            UserProfile p = profile(Gender.MALE, 30, 175, 70, ActivityLevel.SEDENTARY, HealthGoal.WEIGHT_GAIN);
            assertThrows(ValidationException.class, () -> p.setWeightKg(-5), "negative weight");
            assertThrows(ValidationException.class, () -> p.setWeightKg(0), "zero weight");
            assertThrows(ValidationException.class, () -> p.setHeightCm(-170), "negative height");
            assertThrows(ValidationException.class, () -> p.setHeightCm(300), "height too large");
            assertThrows(ValidationException.class, () -> p.setAge(10), "age too low");
            assertThrows(ValidationException.class, () -> p.setAge(150), "age too high");
            assertThrows(ValidationException.class, () -> p.setName("   "), "empty name");
            assertThrows(ValidationException.class, () -> p.setName("R2D2"), "digits in name");
            assertEquals(70.0, p.getWeightKg(), "weight unchanged after failed updates");
            assertEquals("Anne-Marie O'Neil", UserProfile.validateName("  Anne-Marie   O'Neil "), "name cleaned");
        });
    }
}
