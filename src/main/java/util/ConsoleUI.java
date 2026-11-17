package util;

import java.util.List;

public final class ConsoleUI {

    private static final int BOX_WIDTH = 46;
    private static final int MAX_CELL_WIDTH = 30;

    private ConsoleUI() {

    }

    public static void printHeader(String title) {
        int width = Math.max(BOX_WIDTH, title.length() + 6);
        String line = "═".repeat(width);

        System.out.println();
        System.out.println("╔" + line + "╗");
        System.out.println("║" + padRight("   " + title, width) + "║");
        System.out.println("╚" + line + "╝");
        System.out.println();
    }

    public static void printMenu(String title, String... options) {
        printHeader(title);
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }
        System.out.println();
    }

    public static void printSuccess(String message) {
        System.out.println("[OK] " + message);
    }

    public static void printError(String message) {
        System.err.println("[ERROR] " + message);
    }

    public static void printInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void printSeparator() {
        System.out.println("-".repeat(BOX_WIDTH + 2));
    }

    public static void printTable(List<String> headers, List<List<String>> rows) {
        if (rows == null || rows.isEmpty()) {
            printInfo("No se encontraron resultados");
            return;
        }

        int columns = headers.size();
        int[] widths = new int[columns];

        for (int i = 0; i < columns; i++) {
            widths[i] = cell(headers.get(i)).length();
        }
        for (List<String> row : rows) {
            for (int i = 0; i < columns; i++) {
                widths[i] = Math.max(widths[i], cell(getCell(row, i)).length());
            }
        }

        String border = buildBorder(widths);

        System.out.println(border);
        System.out.println(buildRow(headers, widths));
        System.out.println(border);
        for (List<String> row : rows) {
            System.out.println(buildRow(row, widths));
        }
        System.out.println(border);
        System.out.println(rows.size() + " registro(s)");
    }

    public static void pause() {
        System.out.println();
        InputReader.waitForEnter("Presione Enter para continuar...");
    }

    private static String buildBorder(int[] widths) {
        StringBuilder border = new StringBuilder("+");
        for (int width : widths) {
            border.append("-".repeat(width + 2)).append("+");
        }
        return border.toString();
    }

    private static String buildRow(List<String> row, int[] widths) {
        StringBuilder line = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            line.append(" ").append(padRight(cell(getCell(row, i)), widths[i])).append(" |");
        }
        return line.toString();
    }

    private static String getCell(List<String> row, int index) {
        return index < row.size() ? row.get(index) : null;
    }

    private static String cell(String value) {
        if (value == null) {
            return "-";
        }
        if (value.length() > MAX_CELL_WIDTH) {
            return value.substring(0, MAX_CELL_WIDTH - 3) + "...";
        }
        return value;
    }

    private static String padRight(String text, int width) {
        return text + " ".repeat(Math.max(0, width - text.length()));
    }
}