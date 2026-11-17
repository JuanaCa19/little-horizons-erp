package integration;

import java.lang.reflect.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** A private JDBC session whose temporary tables shadow the real application tables. */
final class TemporaryDatabase implements AutoCloseable {
    private final Connection connection;
    TemporaryDatabase(String... tables) throws Exception {
        Properties config=new Properties();
        try(var reader=Files.newBufferedReader(Path.of("config.txt"))) { config.load(reader); }
        connection=DriverManager.getConnection(config.getProperty("url"),config.getProperty("user"),config.getProperty("password"));
        try {
            for(String table:tables) {
                String ddl;
                try(PreparedStatement ps=connection.prepareStatement("SHOW CREATE TABLE "+table);ResultSet rs=ps.executeQuery()) { rs.next();ddl=rs.getString(2); }
                ddl=ddl.lines().filter(line -> !line.contains("FOREIGN KEY")).collect(Collectors.joining("\n"))
                        .replaceFirst("CREATE TABLE","CREATE TEMPORARY TABLE").replace(",\n)","\n)");
                try(PreparedStatement ps=connection.prepareStatement(ddl)) { ps.execute(); }
            }
        } catch(Exception e) { connection.close();throw e; }
    }
    Supplier<Connection> connections() {
        return () -> (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},(proxy,method,args) -> {
            if(method.getName().equals("close")) return null;
            try { return method.invoke(connection,args); } catch(InvocationTargetException e) { throw e.getCause(); }
        });
    }
    void execute(String sql) throws SQLException {
        try(PreparedStatement ps=connection.prepareStatement(sql)) { ps.executeUpdate(); }
        if(!connection.getAutoCommit()) connection.commit();
    }
    @Override public void close() throws SQLException { connection.close(); }
}
