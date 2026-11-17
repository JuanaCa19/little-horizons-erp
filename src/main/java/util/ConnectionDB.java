package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static util.AppLogger.logError;
import static util.ConfigLoader.getCredentialsDB;

public class ConnectionDB {

    public static Connection getConnection() {
        Connection con = null;
        List<String> credentials = getCredentialsDB();

        if (credentials == null || credentials.size() < 3) {
            System.err.println("Credenciales de base de datos incompletas en config.txt.");
            return null;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(credentials.get(2), credentials.get(0), credentials.get(1));
        } catch (ClassNotFoundException e) {
            logError("Error al obtener controlador JDBC", e);
            System.err.println("Error al obtener controlador: " + e.getMessage());
        } catch (SQLException e) {
            logError("Error al conectar con la BD", e);
            System.err.println("Error al conectar con la BD: " + e.getMessage());
        }

        return con;
    }
}
