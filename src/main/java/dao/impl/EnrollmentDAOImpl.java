package dao.impl;

import dao.EnrollmentDAO;
import model.Enrollment;
import model.enums.EnrollmentStatus;
import util.exception.DataAccessException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static util.AppLogger.logError;
import static util.ConnectionDB.getConnection;

public class EnrollmentDAOImpl implements EnrollmentDAO {

    private Enrollment mapRow(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(rs.getInt("id_matricula"));
        enrollment.setStudentId(rs.getInt("id_estudiante"));
        enrollment.setGradeId(rs.getInt("id_grado"));
        enrollment.setAcademicYear(rs.getInt("anio_academico"));
        enrollment.setStatus(EnrollmentStatus.fromDbValue(rs.getString("estado_matricula")));
        enrollment.setEnrollmentDate(rs.getObject("fecha_matricula", LocalDate.class));
        enrollment.setWithdrawalDate(rs.getObject("fecha_retiro", LocalDate.class));
        enrollment.setNotes(rs.getString("observaciones"));
        return enrollment;
    }

    private void setEnrollmentParameters(PreparedStatement ps, Enrollment enrollment) throws SQLException {
        ps.setInt(1, enrollment.getStudentId());
        ps.setInt(2, enrollment.getGradeId());
        ps.setInt(3, enrollment.getAcademicYear());
        ps.setString(4, enrollment.getStatus().getDbValue());
        ps.setDate(5, Date.valueOf(enrollment.getEnrollmentDate()));
        ps.setObject(6, enrollment.getWithdrawalDate(), Types.DATE);
        ps.setString(7, enrollment.getNotes());
    }

    @Override
    public int create(Enrollment value) {
        try (Connection connection = getConnection()) {
            return create(value, connection);
        } catch (SQLException e) {
            logError("EnrollmentDAOImpl.create", e);
            throw new DataAccessException("No se pudo crear el registro", e);
        }
    }

    @Override
    public int create(Enrollment value, Connection connection) {
        String sql = "INSERT INTO matriculas (id_estudiante, id_grado, anio_academico, estado_matricula, " +
                "fecha_matricula, fecha_retiro, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setEnrollmentParameters(ps, value);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DataAccessException("No se obtuvo el id generado");
        } catch (SQLException e) {
            logError("EnrollmentDAOImpl.create", e);
            throw new DataAccessException("No se pudo crear el registro", e);
        }
    }

    @Override
    public Optional<Enrollment> findById(Integer id) {
        String sql = "SELECT * FROM matriculas WHERE id_matricula = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            logError("EnrollmentDAOImpl.findById", e);
            throw new DataAccessException("No se pudo realizar la consulta", e);
        }
    }

    @Override
    public Optional<Enrollment> findByStudentAndYear(Integer studentId, Integer academicYear) {
        String sql = "SELECT * FROM matriculas WHERE id_estudiante = ? AND anio_academico = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, academicYear);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            logError("EnrollmentDAOImpl.findByStudentAndYear", e);
            throw new DataAccessException("No se pudo realizar la consulta", e);
        }
    }

    @Override
    public List<Enrollment> findByStudentId(Integer studentId) {
        String sql = "SELECT * FROM matriculas WHERE id_estudiante = ? ORDER BY anio_academico DESC";
        List<Enrollment> results = new ArrayList<>();

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
            logError("EnrollmentDAOImpl.findByStudentId", e);
            throw new DataAccessException("No se pudo realizar la consulta", e);
        }
    }

    @Override
    public List<Enrollment> findByYearAndGrade(Integer academicYear, Integer gradeId) {
        String sql = "SELECT * FROM matriculas WHERE anio_academico = ? AND id_grado = ?";
        List<Enrollment> results = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, academicYear);
            ps.setInt(2, gradeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            logError("EnrollmentDAOImpl.findByYearAndGrade", e);
            throw new DataAccessException("No se pudo realizar la consulta", e);
        }
    }

    @Override
    public void updateStatus(EnrollmentStatus status, LocalDate withdrawalDate, Integer id) {
        String sql = "UPDATE matriculas SET estado_matricula = ?, fecha_retiro = ? WHERE id_matricula = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, status.getDbValue());
            ps.setObject(2, withdrawalDate, Types.DATE);
            ps.setInt(3, id);

            if (ps.executeUpdate() < 1) {
                throw new DataAccessException("No se encontro o modifico el registro");
            }
        } catch (SQLException e) {
            logError("EnrollmentDAOImpl.updateStatus", e);
            throw new DataAccessException("No se pudo modificar el registro", e);
        }
    }
}
