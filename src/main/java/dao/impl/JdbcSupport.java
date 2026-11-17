package dao.impl;

import java.sql.*;
import java.util.*;
import java.util.function.Supplier;
import util.ConnectionDB;
import util.exception.DataAccessException;
import static util.AppLogger.logError;

final class JdbcSupport {
    @FunctionalInterface interface Binder { void bind(PreparedStatement statement) throws SQLException; }
    @FunctionalInterface interface Mapper<T> { T map(ResultSet result) throws SQLException; }
    @FunctionalInterface interface Work<T> { T run(Connection connection) throws SQLException; }
    private final Supplier<Connection> connections;
    JdbcSupport() { this(ConnectionDB::getConnection); }
    JdbcSupport(Supplier<Connection> connections) { this.connections = Objects.requireNonNull(connections); }
    <T> T execute(Work<T> work) {
        try (Connection connection = connections.get()) {
            if (connection == null) throw new DataAccessException("No se pudo conectar con la base de datos.");
            return work.run(connection);
        } catch (SQLException e) { throw failure(e); }
    }
    static DataAccessException failure(SQLException error) {
        logError("JDBC database operation", error);
        return new DataAccessException("No se pudo completar la operacion en la base de datos.", error);
    }
    static <T> List<T> query(Connection connection, String sql, Binder binder, Mapper<T> mapper) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> values = new ArrayList<>();
                while (rs.next()) values.add(mapper.map(rs));
                return values;
            }
        } catch (SQLException e) { throw failure(e); }
    }
    static int insert(Connection connection, String sql, Binder binder) {
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            binder.bind(ps);
            if (ps.executeUpdate() != 1) throw new DataAccessException("No se pudo crear el registro.");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            throw new DataAccessException("No se obtuvo el identificador generado.");
        } catch (SQLException e) { throw failure(e); }
    }
    static void update(Connection connection, String sql, Binder binder) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            binder.bind(ps);
            if (ps.executeUpdate() != 1) throw new DataAccessException("Registro no encontrado o sin cambios.");
        } catch (SQLException e) { throw failure(e); }
    }
}
