package util;

import util.exception.CancelledException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;

public final class InputReader {

    private static final Scanner CONSOLE = new Scanner(System.in);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
            .ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter
            .ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    private InputReader() {
    }

    private static String readLine(String message) {
        System.out.print(message);
        String input = CONSOLE.nextLine();
        if (input.trim().equalsIgnoreCase("cancelar")) {
            throw new CancelledException();
        }
        return input;
    }

    public static int readInt(String message, int min, int max) {
        while (true) {
            String input = readLine(message).trim();
            try {
                int number = Integer.parseInt(input);
                if (number >= min && number <= max) {
                    return number;
                }
                System.err.println("El número debe estar entre " + min + " y " + max);
            } catch (NumberFormatException e) {
                System.err.println("Ingrese un número entero válido");
            }
        }
    }

    public static BigDecimal readDecimal(String message, BigDecimal min, BigDecimal max, int maxScale) {
        while (true) {
            String input = readLine(message).trim().replace(',', '.');
            try {
                BigDecimal value = new BigDecimal(input);
                if (value.compareTo(min) >= 0 && value.compareTo(max) <= 0 && value.stripTrailingZeros().scale() <= maxScale) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // Handled by the message below
            }
            System.err.println("Ingrese un valor entre " + min + " y " + max + " con maximo " + maxScale + " decimal(es).");
        }
    }

    public static double readDouble(String message, double min, double max) {
        while (true) {
            String input = readLine(message).trim().replace(',', '.');
            try {
                double number = Double.parseDouble(input);
                if (number >= min && number <= max) {
                    return number;
                }
                System.err.println("El valor debe estar entre " + min + " y " + max);
            } catch (NumberFormatException e) {
                System.err.println("Ingrese un número válido");
            }
        }
    }

    public static String readRequiredText(String message) {
        while (true) {
            String input = readLine(message).trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.err.println("Este campo no puede estar vacío");
        }
    }

    public static String readOptionalText(String message) {
        String input = readLine(message).trim();
        return input.isEmpty() ? null : input;
    }

    public static LocalTime readTime(String message) {
        while (true) {
            String input = readLine(message).trim();
            try {
                return LocalTime.parse(input, TIME_FORMAT);
            } catch (DateTimeParseException e) {
                System.err.println("Hora invalida. Use HH:mm entre 00:00 y 23:59.");
            }
        }
    }

    public static LocalDate readDate(String message) {
        while (true) {
            String input = readLine(message).trim();
            try {
                return LocalDate.parse(input, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.err.println("Fecha inválida. Use el formato dd/MM/yyyy (ej: 20/02/2026)");
            }
        }
    }

    public static LocalDate readDateInRange(String message, LocalDate min, LocalDate max) {
        while (true) {
            LocalDate date = readDate(message);
            if (date.isBefore(min)) {
                System.err.println("La fecha no puede ser anterior a " + min.format(DATE_FORMAT));
            } else if (date.isAfter(max)) {
                System.err.println("La fecha no puede ser posterior a " + max.format(DATE_FORMAT));
            } else {
                return date;
            }
        }
    }

    public static String readChoice(String message, String... allowedValues) {
        while (true) {
            String input = readLine(message).trim();
            for (String allowed : allowedValues) {
                if (allowed.equalsIgnoreCase(input)) {
                    return allowed;
                }
            }
            System.err.println("Opción no válida. Valores permitidos: " + String.join(", ", allowedValues));
        }
    }

    public static boolean readYesNo(String message) {
        while (true) {
            String input = readLine(message + " (si/no): ").trim();
            if (input.equalsIgnoreCase("si") || input.equalsIgnoreCase("sí") || input.equalsIgnoreCase("s")) {
                return true;
            }
            if (input.equalsIgnoreCase("no") || input.equalsIgnoreCase("n")) {
                return false;
            }
            System.err.println("Escriba si o no");
        }
    }

    public static void waitForEnter(String message) {
        System.out.print(message);
        CONSOLE.nextLine();
    }
}
