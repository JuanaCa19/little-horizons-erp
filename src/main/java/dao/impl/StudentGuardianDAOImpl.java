package dao.impl;

import dao.StudentGuardianDAO;
import model.StudentGuardian;
import util.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static util.AppLogger.logError;
import static util.ConnectionDB.getConnection;

public class StudentGuardianDAOImpl implements StudentGuardianDAO {

    private StudentGuardian mapRow(ResultSet rs) throws SQLException {
        StudentGuardian relation = new StudentGuardian();
        relation.setStudentId(rs.getInt("id_estudiante"));
        relation.setGuardianId(rs.getInt("id_acudiente"));
        relation.setRelationShip(rs.getString("parentesco"));
        relation.setPrimary(rs.getBoolean("es_principal"));
        relation.setFinancialResponsible(rs.getBoolean("es_responsable_financiero"));
        relation.setEmergencyContact(rs.getBoolean("es_contacto_emergencia"));
        return relation;
    }

    @Override
    public void link(StudentGuardian relation) {
        try (Connection connection = getConnection()) {
            link(relation, connection);
        } catch (SQLException e) {
            logError("StudentGuardianDAOImpl.link", e);
            throw new DataAccessException("No se pudo vincular al acudiente", e);
        }
    }

    @Override
    public void link(StudentGuardian relation, Connection connection) {
        String sql = "INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, parentesco, " +
                "es_principal, es_responsable_financiero, es_contacto_emergencia) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, relation.getStudentId());
            ps.setInt(2, relation.getGuardianId());
            ps.setString(3, relation.getRelationShip());
            ps.setBoolean(4, relation.isPrimary());
            ps.setBoolean(5, relation.isFinancialResponsible());
            ps.setBoolean(6, relation.isEmergencyContact());
            ps.executeUpdate();
        } catch (SQLException e) {
            logError("StudentGuardianDAOImpl.link", e);
            throw new DataAccessException("No se pudo vincular al acudiente", e);
        }
    }

    @Override
    public List<StudentGuardian> findByStudentId(Integer studentId) {
        String sql = "SELECT * FROM estudiante_acudiente WHERE id_estudiante = ? " +
                "ORDER BY es_principal DESC, id_acudiente";
        List<StudentGuardian> results = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            logError("StudentGuardianDAOImpl.findByStudentId", e);
            throw new DataAccessException("No se pudo realizar la consulta", e);
        }
    }

    @Override
    public List<StudentGuardian> findFinancialResponsibles(Integer studentId) {
        String sql = "SELECT * FROM estudiante_acudiente WHERE id_estudiante = ? AND es_responsable_financiero = TRUE " +
                "ORDER BY es_principal DESC, id_acudiente";
        List<StudentGuardian> results = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            logError("StudentGuardianDAOImpl.findFinancialResponsibles", e);
            throw new DataAccessException("No se pudo realizar la consulta", e);
        }
    }

    @Override
    public List<StudentGuardian> findByGuardianId(Integer guardianId) {
        String sql = "SELECT * FROM estudiante_acudiente WHERE id_acudiente = ?";
        List<StudentGuardian> results = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, guardianId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            logError("StudentGuardianDAOImpl.findByGuardianId", e);
            throw new DataAccessException("No se pudo realizar la consulta", e);
        }
    }

    @Override
    public Optional<StudentGuardian> findByIds(Integer studentId, Integer guardianId) {
        String sql = "SELECT * FROM estudiante_acudiente WHERE id_estudiante = ? AND id_acudiente = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, guardianId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logError("StudentGuardianDAOImpl.findByIds", e);
            throw new DataAccessException("No se pudo buscar la relacion", e);
        }
    }

    @Override
    public void updateRelation(StudentGuardian relation) {
        String sql = "UPDATE estudiante_acudiente SET parentesco = ?, es_responsable_financiero = ?, " +
                "es_contacto_emergencia = ? WHERE id_estudiante = ? AND id_acudiente = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, relation.getRelationShip());
            ps.setBoolean(2, relation.isFinancialResponsible());
            ps.setBoolean(3, relation.isEmergencyContact());
            ps.setInt(4, relation.getStudentId());
            ps.setInt(5, relation.getGuardianId());

            if (ps.executeUpdate() < 1) {
                throw new DataAccessException("No se encontro o modifico el registro");
            }
        } catch (SQLException e) {
            logError("StudentGuardianDAOImpl.updateRelation", e);
            throw new DataAccessException("No se pudo modificar el registro", e);
        }
    }

    @Override
    public void unlink(Integer studentId, Integer guardianId) {
        String sql = "DELETE FROM estudiante_acudiente WHERE id_estudiante = ? AND id_acudiente = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, guardianId);

            if (ps.executeUpdate() < 1) {
                throw new DataAccessException("No se encontro o modifico el registro");
            }
        } catch (SQLException e) {
            logError("StudentGuardianDAOImpl.unlink", e);
            throw new DataAccessException("No se pudo modificar el registro", e);
        }
    }

    @Override
    public void changePrimary(Integer studentId, Integer guardianId) {
        String lock = "SELECT id_estudiante FROM estudiantes WHERE id_estudiante = ? FOR UPDATE";
        String clear = "UPDATE estudiante_acudiente SET es_principal = FALSE WHERE id_estudiante = ? AND es_principal = TRUE";
        String mark = "UPDATE estudiante_acudiente SET es_principal = TRUE WHERE id_estudiante = ? AND id_acudiente = ?";

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement ps = connection.prepareStatement(lock)) {
                    ps.setInt(1, studentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new DataAccessException("Estudiante no encontrado");
                        }
                    }
                }

                try (PreparedStatement ps = connection.prepareStatement(clear)) {
                    ps.setInt(1, studentId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = connection.prepareStatement(mark)) {
                    ps.setInt(1, studentId);
                    ps.setInt(2, guardianId);

                    if (ps.executeUpdate() != 1) {
                        throw new DataAccessException("El acudiente no esta vinculado al estudiante");
                    }
                }

                connection.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (SQLException e) {
            logError("StudentGuardianDAOImpl.changePrimary", e);
            throw new DataAccessException("No se pudo cambiar el acudiente principal", e);
        }
    }
}
