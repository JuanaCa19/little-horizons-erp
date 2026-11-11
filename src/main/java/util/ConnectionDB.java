package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static util.ConfigLoader.getCredentialsDB;

public class ConnectionDB {

    public static Connection getConnection(){

        Connection con = null;

        List<String> credentials = getCredentialsDB();

        try{

            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(credentials.get(0),credentials.get(1),credentials.get(2));

        }catch (NullPointerException e){

            System.err.println("Error al obtener credenciales: " + e.getMessage());

        }catch (ClassNotFoundException e){

            System.err.println("Error al obtener controlador: " + e.getMessage());

        }catch (SQLException e){

            System.err.println("Error al conectar con la BD: " + e.getMessage());

        }
        return con;
    }
}
