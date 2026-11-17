package com.nutriplanner.ui;

import com.nutriplanner.model.DailyMealPlan;
import com.nutriplanner.model.User;
import com.nutriplanner.service.NutritionReport;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Shows nutrition reports in the terminal and can save them as text files.
 */
public class ReportMenu extends Menu {

    private static final int SUMMARY_DAYS = 7;

    private final User user;

    public ReportMenu(AppContext context, User user) {
        super(context);
        this.user = user;
    }

    @Override
    protected String getTitle() {
        return "NUTRITION REPORTS";
    }

    @Override
    protected String[] getOptions() {
        return new String[] {
            "Today's nutrition report",
            "Nutrition report for another date",
            "Last " + SUMMARY_DAYS + " days summary",
            "Back to main menu"
        };
    }

    @Override
    protected boolean handleChoice(int choice) {
        NutritionReport report = context.getReport();
        switch (choice) {
            case 1:
                showDaily(report, context.today());
                return true;
            case 2:
                view.blankLine();
                LocalDate date = input.readDate("Enter a date (DD-MM-YYYY), or today, yesterday, tomorrow [today]: ",
                        context.today());
                showDaily(report, date);
                return true;
            case 3:
                LocalDate today = context.today();
                List<DailyMealPlan> plans = context.getMealPlanner()
                        .getPlansBetween(user.getUsername(), today.minusDays(SUMMARY_DAYS - 1), today);
                String text = report.buildWeeklySummary(user, plans, context.now());
                showAndOfferSave(text, NutritionReport.reportFileName(user.getUsername(), today, SUMMARY_DAYS + "day_summary"));
                return true;
            default:
                return false;
        }
    }

    private void showDaily(NutritionReport report, LocalDate date) {
        DailyMealPlan plan = context.getMealPlanner().getPlan(user.getUsername(), date);
        String text = report.buildDailyReport(user, plan, context.now());
        showAndOfferSave(text, NutritionReport.reportFileName(user.getUsername(), date, "daily_report"));
    }

    private void showAndOfferSave(String text, String fileName) {
        view.blankLine();
        view.printBlock(text);
        view.blankLine();
        if (input.readYesNo("Save this report as a text file?")) {
            Path file = context.getStorage().saveReport(fileName, text);
            view.success("Report saved as a text file:");
            view.println("  " + file.toAbsolutePath().normalize());
        }
    }
}
