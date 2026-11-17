package com.nutriplanner.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Formatting helpers so that numbers and dates look the same on every screen.
 * Locale.US is used so decimals always use a dot, whatever the computer's settings.
 */
public final class Fmt {

    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter INPUT_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH);

    /** Width of the "=" and "-" lines used in headers and reports. */
    public static final int LINE_WIDTH = 76;

    private Fmt() {
        // Utility class: no objects needed.
    }

    /** Whole kilocalories with a thousands separator, for example "1,850". */
    public static String kcal(double value) {
        return String.format(Locale.US, "%,d", Math.round(value));
    }

    /** Calories with a sign, for example "+500", "-500" or "0". */
    public static String signedKcal(double value) {
        long rounded = Math.round(value);
        return rounded > 0 ? "+" + kcal(rounded) : kcal(rounded);
    }

    /** One decimal place, for example "12.5". */
    public static String oneDecimal(double value) {
        return String.format(Locale.US, "%.1f", value);
    }

    /** A number without unnecessary zeros, for example 2.0 -> "2" and 0.50 -> "0.5". */
    public static String number(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    /** "Tue, 17 Nov 2026" */
    public static String date(LocalDate date) {
        return LONG_DATE.format(date);
    }

    /** "17 Nov 2026" */
    public static String shortDate(LocalDate date) {
        return SHORT_DATE.format(date);
    }

    /** "17-11-2026", the format users type dates in. */
    public static String inputDate(LocalDate date) {
        return INPUT_DATE.format(date);
    }

    /** "17 Nov 2026, 11:20" */
    public static String dateTime(LocalDateTime dateTime) {
        return DATE_TIME.format(dateTime);
    }

    /** Whole-number percentage of part in whole, or 0 when whole is zero. */
    public static int percent(double part, double whole) {
        if (whole <= 0) {
            return 0;
        }
        return (int) Math.round(part * 100.0 / whole);
    }

    /** A simple text progress bar, for example "[#######-------------]". */
    public static String progressBar(double part, double whole, int width) {
        int filled = whole <= 0 ? 0 : (int) Math.round(width * Math.max(0, part) / whole);
        filled = Math.min(width, filled);
        return "[" + repeat('#', filled) + repeat('-', width - filled) + "]";
    }

    public static String repeat(char c, int count) {
        return count <= 0 ? "" : String.valueOf(c).repeat(count);
    }

    /** Pads text with spaces on the right to the given width. */
    public static String padRight(String text, int width) {
        return text.length() >= width ? text : text + repeat(' ', width - text.length());
    }

    /** Pads text with spaces on the left to the given width. */
    public static String padLeft(String text, int width) {
        return text.length() >= width ? text : repeat(' ', width - text.length()) + text;
    }

    /** Centres text within the given width. */
    public static String center(String text, int width) {
        int left = Math.max(0, (width - text.length()) / 2);
        return repeat(' ', left) + text;
    }

    /**
     * Splits a long message into lines of at most 'width' characters, breaking
     * only between words. The first line starts with 'prefix'; the following
     * lines are indented by the same amount so the text lines up.
     */
    public static List<String> wrap(String prefix, String text, int width) {
        List<String> lines = new ArrayList<>();
        String indent = repeat(' ', prefix.length());
        StringBuilder line = new StringBuilder(prefix);
        boolean lineHasWord = false;
        for (String word : text.trim().split("\\s+")) {
            if (lineHasWord && line.length() + 1 + word.length() > width) {
                lines.add(line.toString());
                line = new StringBuilder(indent);
                lineHasWord = false;
            }
            if (lineHasWord) {
                line.append(' ');
            }
            line.append(word);
            lineHasWord = true;
        }
        lines.add(line.toString());
        return lines;
    }

    /** Shortens text that is too long, ending it with "...". */
    public static String truncate(String text, int width) {
        if (text.length() <= width) {
            return text;
        }
        return width <= 3 ? text.substring(0, width) : text.substring(0, width - 3) + "...";
    }
}
