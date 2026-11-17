package view;

import util.exception.CancelledException;
import util.exception.DataAccessException;
import util.exception.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import static util.AppLogger.logError;
import static util.ConsoleUI.pause;
import static util.ConsoleUI.printError;
import static util.ConsoleUI.printInfo;
import static util.ConsoleUI.printMenu;
import static util.InputReader.readDate;
import static util.InputReader.readInt;
import static util.InputReader.readOptionalText;
import static util.InputReader.readRequiredText;
import static util.InputReader.readYesNo;

final class ViewSupport {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private ViewSupport() {
    }

    static void menu(String title, String exit, String[] labels, Runnable... actions) {
        if (labels.length != actions.length) {
            throw new IllegalArgumentException("Menu actions do not match labels");
        }

        String[] options = Arrays.copyOf(labels, labels.length + 1);
        options[labels.length] = exit;

        while (true) {
            printMenu(title, options);
            printInfo("Escriba 'cancelar' en cualquier momento para cancelar la operacion actual.");

            try {
                int option = readInt("Seleccione una opcion: ", 1, options.length);
                if (option == options.length) {
                    return;
                }
                actions[option - 1].run();
            } catch (CancelledException e) {
                printInfo("Operacion cancelada.");
            } catch (ValidationException | DataAccessException e) {
                printError(e.getMessage());
                pause();
            } catch (NoSuchElementException e) {
                throw e;
            } catch (RuntimeException e) {
                logError(title, e);
                printError("No se pudo completar la operacion. Revise error_log.txt.");
                pause();
            }
        }
    }

    static Runnable action(Runnable operation) {
        return () -> {
            operation.run();
            pause();
        };
    }

    static int readId(String label) {
        return readInt(label + ": ", 1, Integer.MAX_VALUE);
    }

    static String value(Object val) {
        if (val == null) {
            return "-";
        }
        if (val instanceof LocalDate date) {
            return DATE_FORMATTER.format(date);
        }
        if (val instanceof Boolean flag) {
            return flag ? "Si" : "No";
        }
        return val.toString();
    }

    static void field(String label, Object fieldValue) {
        System.out.println(label + ": " + value(fieldValue));
    }

    static List<String> row(Object... values) {
        return Arrays.stream(values).map(ViewSupport::value).toList();
    }

    static String required(String label, String current) {
        if (current == null) {
            return readRequiredText(label + ": ");
        }
        String input = readOptionalText(label + " [" + current + "]: ");
        return input == null ? current : input;
    }

    static String optional(String label, String current) {
        String input = readOptionalText(label + " [" + value(current) + "] (opcional, - para borrar): ");
        return "-".equals(input) ? null : input == null ? current : input;
    }

    static LocalDate date(String label, LocalDate current) {
        if (current != null && !readYesNo("Cambiar " + label + " [" + value(current) + "]")) {
            return current;
        }
        return readDate(label + " (dd/MM/yyyy): ");
    }

    static <T> T choose(String label, T[] values, Function<T, String> display, T current) {
        if (current != null && !readYesNo("Cambiar " + label + " [" + display.apply(current) + "]")) {
            return current;
        }
        printMenu(label, Arrays.stream(values).map(display).toArray(String[]::new));
        return values[readInt("Seleccione una opcion: ", 1, values.length) - 1];
    }
}
