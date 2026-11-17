package dao.impl;

import dao.GradeDAO;
import model.Grade;
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

public class GradeDAOImpl implements GradeDAO {

    private Grade mapRow(ResultSet rs) throws SQLException {
        Grade grade = new Grade();
        grade.setId(rs.getInt("id_grado"));
        grade.setName(rs.getString("nombre_grado"));
        grade.setNumericLevel(rs.getInt("nivel_numerico"));
        return grade;
    }

    @Override
    public Optional<Grade> findById(Integer id) {
        String sql = "SELECT * FROM grados WHERE id_grado = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            logError("GradeDAOImpl.findById", e);
            throw new DataAccessException("No se pudo realizar la consulta", e);
        }
    }

    @Override
    public List<Grade> findAll() {
        String sql = "SELECT * FROM grados ORDER BY nivel_numerico";
        List<Grade> results = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            logError("GradeDAOImpl.findAll", e);
            throw new DataAccessException("No se pudo realizar la consulta", e);
        }
    }
}
