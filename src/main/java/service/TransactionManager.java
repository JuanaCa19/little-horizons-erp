package service;

import util.ConnectionDB;
import util.exception.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Supplier;

import static util.AppLogger.logError;

final class TransactionManager {

    @FunctionalInterface
    interface Work<T> {
        T execute(Connection connection) throws SQLException;
    }

    private final Supplier<Connection> connections;

    TransactionManager() {
        this(ConnectionDB::getConnection);
    }

    TransactionManager(Supplier<Connection> connections) {
        this.connections = Objects.requireNonNull(connections);
    }

    <T> T execute(Work<T> work) {
        try (Connection connection = connections.get()) {
            if (connection == null) {
                throw new DataAccessException("No se pudo conectar con la base de datos.");
            }
            connection.setAutoCommit(false);
            try {
                T result = work.execute(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (SQLException e) {
            logError("TransactionManager.execute", e);
            throw new DataAccessException("No se pudo completar la transaccion.", e);
        }
    }
}
