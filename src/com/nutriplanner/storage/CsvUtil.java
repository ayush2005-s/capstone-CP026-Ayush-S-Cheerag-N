package com.nutriplanner.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes single lines in CSV (comma-separated values) format.
 *
 * A value that contains a comma or a double quote is wrapped in double quotes,
 * and any double quote inside it is written twice. For example, the value
 *   Curd rice, homemade
 * is stored as
 *   "Curd rice, homemade"
 */
public final class CsvUtil {

    private CsvUtil() {
        // Utility class: no objects needed.
    }

    /** Joins values into one CSV line. */
    public static String toLine(List<String> values) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(escape(values.get(i)));
        }
        return line.toString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes = value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r");
        if (!needsQuotes) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    /**
     * Splits one CSV line into its values.
     *
     * @throws IllegalArgumentException when a quoted value is never closed
     */
    public static List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    boolean escapedQuote = i + 1 < line.length() && line.charAt(i + 1) == '"';
                    if (escapedQuote) {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (inQuotes) {
            throw new IllegalArgumentException("a quoted value is not closed");
        }
        values.add(current.toString());
        return values;
    }
}
