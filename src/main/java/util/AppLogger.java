package util;

import java.io.*;
import java.text.Format;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppLogger {

    public static void logError(String messageEntry,Throwable throwable){

        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("error_log.txt",true))){


            String message = buildError(messageEntry,throwable);

            bufferedWriter.write(message);
            bufferedWriter.newLine();


        } catch (IOException e) {
            System.out.println("Error al intentar escribir en el archivo: " + e.getMessage());
        }
    }

    public static String buildError(String message,Throwable throwable){

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        StringBuilder error = new StringBuilder();

        error.append("[")
                .append(LocalDateTime.now().format(format))
                .append("] [ERROR] - ")
                .append(message)
                .append(" - ")
                .append(throwable);

        return error.toString();
    }
}
