package com.nutriplanner.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a neatly aligned text table for the terminal.
 *
 * Example:
 * <pre>
 *   TextTable table = new TextTable()
 *       .addColumn("Food", 20, TextTable.Align.LEFT)
 *       .addColumn("kcal", 6, TextTable.Align.RIGHT);
 *   table.addRow("Idli", "58");
 *   System.out.print(table.render());
 * </pre>
 */
public class TextTable {

    /** Text alignment inside a column. Numbers are usually right-aligned. */
    public enum Align { LEFT, RIGHT }

    private static final String NEWLINE = System.lineSeparator();

    private final List<String> headers = new ArrayList<>();
    private final List<Integer> widths = new ArrayList<>();
    private final List<Align> alignments = new ArrayList<>();
    private final List<String[]> rows = new ArrayList<>();   // a null row means "separator line"
    private String indent = "";
    private String gap = "  ";

    /** Indents every line of the table by the given number of spaces. */
    public TextTable indent(int spaces) {
        this.indent = Fmt.repeat(' ', spaces);
        return this;
    }

    /** Sets the number of spaces between columns (default 2). */
    public TextTable gap(int spaces) {
        this.gap = Fmt.repeat(' ', spaces);
        return this;
    }

    public TextTable addColumn(String header, int width, Align align) {
        headers.add(header);
        widths.add(Math.max(width, header.length()));
        alignments.add(align);
        return this;
    }

    public TextTable addRow(String... cells) {
        if (cells.length != headers.size()) {
            throw new IllegalArgumentException("Expected " + headers.size() + " cells but got " + cells.length);
        }
        rows.add(cells.clone());
        return this;
    }

    /** Adds a dashed line, for example before a totals row. */
    public TextTable addSeparator() {
        rows.add(null);
        return this;
    }

    public boolean hasRows() {
        return !rows.isEmpty();
    }

    /** Returns the whole table as text, one line per row, ending with a newline. */
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append(formatRow(headers.toArray(new String[0]))).append(NEWLINE);
        sb.append(separatorLine()).append(NEWLINE);
        for (String[] row : rows) {
            sb.append(row == null ? separatorLine() : formatRow(row)).append(NEWLINE);
        }
        return sb.toString();
    }

    private String separatorLine() {
        int total = 0;
        for (int width : widths) {
            total += width;
        }
        total += gap.length() * (widths.size() - 1);
        return indent + Fmt.repeat('-', total);
    }

    private String formatRow(String[] cells) {
        StringBuilder line = new StringBuilder(indent);
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                line.append(gap);
            }
            String text = Fmt.truncate(cells[i] == null ? "" : cells[i], widths.get(i));
            line.append(alignments.get(i) == Align.RIGHT
                    ? Fmt.padLeft(text, widths.get(i))
                    : Fmt.padRight(text, widths.get(i)));
        }
        return line.toString().stripTrailing();
    }
}
