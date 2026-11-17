package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logError(String messageEntry, Throwable throwable) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("error_log.txt", true))) {
            String message = buildError(messageEntry, throwable);
            bufferedWriter.write(message);
            bufferedWriter.newLine();
        } catch (IOException e) {
            System.err.println("Error al intentar escribir en el archivo de log: " + e.getMessage());
        }
    }

    public static String buildError(String message, Throwable throwable) {
        return "[" + LocalDateTime.now().format(FORMATTER) + "] [ERROR] - " + message + " - " + throwable;
    }
}
