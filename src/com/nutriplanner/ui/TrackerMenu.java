package com.nutriplanner.ui;

import com.nutriplanner.model.DailySummary;
import com.nutriplanner.model.MealType;
import com.nutriplanner.model.User;
import com.nutriplanner.util.Fmt;

import java.time.LocalDate;
import java.util.Map;

/**
 * Shows a day's intake against the user's targets: calorie target, calories
 * consumed, remaining calories, and protein, carbohydrate and fat intake.
 */
public class TrackerMenu extends Menu {

    private final User user;
    private LocalDate date;

    public TrackerMenu(AppContext context, User user) {
        super(context);
        this.user = user;
        this.date = context.today();
    }

    @Override
    protected String getTitle() {
        return "DAILY NUTRITION TRACKER";
    }

    @Override
    protected void printContext() {
        DailySummary summary = context.getTracker().summarize(user.getProfile(),
                context.getMealPlanner().getPlan(user.getUsername(), date));
        view.println("Date: " + Fmt.date(date) + (date.equals(context.today()) ? " (today)" : ""));
        view.blankLine();
        view.field("Daily calorie target", Fmt.kcal(summary.getTargets().getCalorieTarget()) + " kcal");
        view.field("Calories consumed", Fmt.kcal(summary.getConsumed().getCalories()) + " kcal");
        double remaining = summary.getRemainingCalories();
        view.field(remaining >= 0 ? "Remaining calories" : "Over target by", Fmt.kcal(Math.abs(remaining)) + " kcal");
        view.field("Protein intake", Fmt.oneDecimal(summary.getConsumed().getProtein()) + " g of "
                + summary.getTargets().getProteinGrams() + " g");
        view.field("Carbohydrate intake", Fmt.oneDecimal(summary.getConsumed().getCarbohydrates()) + " g of "
                + summary.getTargets().getCarbohydrateGrams() + " g");
        view.field("Fat intake", Fmt.oneDecimal(summary.getConsumed().getFat()) + " g of "
                + summary.getTargets().getFatGrams() + " g");
        view.blankLine();
        view.printBlock(context.getReport().renderComparison(summary));
        view.blankLine();

        StringBuilder byMeal = new StringBuilder("Calories by meal: ");
        boolean first = true;
        for (Map.Entry<MealType, Double> entry : summary.getCaloriesByMeal().entrySet()) {
            byMeal.append(first ? "" : " | ").append(entry.getKey().getLabel()).append(" ")
                    .append(Fmt.kcal(entry.getValue()));
            first = false;
        }
        view.println(byMeal.toString());
        view.blankLine();
        for (String advice : context.getTracker().buildAdvice(summary, user.getProfile().getDietType())) {
            view.printWrapped("- ", advice);
        }
        view.blankLine();
    }

    @Override
    protected String[] getOptions() {
        return new String[] {"Track another date", "Back to main menu"};
    }

    @Override
    protected boolean handleChoice(int choice) {
        if (choice == 1) {
            view.blankLine();
            date = input.readDate("Enter a date (DD-MM-YYYY), or today, yesterday, tomorrow [today]: ", context.today());
            return true;
        }
        return false;
    }
}
