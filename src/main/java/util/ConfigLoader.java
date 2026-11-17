package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigLoader {

    public static List<String> getCredentialsDB() {
        File configTxt = new File("config.txt");
        List<String> credentials = new ArrayList<>();

        try (BufferedReader buffer = new BufferedReader(new FileReader(configTxt))) {
            String line;
            while ((line = buffer.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    credentials.add(parts[1].trim());
                }
            }
            return credentials;
        } catch (IOException e) {
            System.err.println("Error al leer archivo config.txt: " + e.getMessage());
        }
        return null;
    }
}
