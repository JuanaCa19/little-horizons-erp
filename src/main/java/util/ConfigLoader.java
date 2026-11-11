package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigLoader {

    public static List<String> getCredentialsDB(){

        try{
            String[] dates;
            List<String> credentials = new ArrayList<>();

            File configTxt = new File("src/config.txt");

            FileReader reader = new FileReader(configTxt);

            BufferedReader buffer = new BufferedReader(reader);

            String line;

            while((line = buffer.readLine()) != null){
                dates = line.split("=");
                credentials.add(dates[1]);
            }

            buffer.close();
            reader.close();

            return credentials;
        } catch (IOException e) {
            System.err.println("Error al leer archivo config.txt : " + e.getMessage());
        }
        return null;
    }
}
