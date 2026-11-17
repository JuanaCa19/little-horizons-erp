package dao.impl;

import dao.GuardianDAO;
import model.Guardian;
import model.enums.DocumentType;
import util.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static util.AppLogger.logError;
import static util.ConnectionDB.getConnection;

public class GuardianDAOImpl implements GuardianDAO {

    static Guardian mapRow(ResultSet rs) throws SQLException {
        Guardian guardian = new Guardian();
        guardian.setId(rs.getInt("id_acudiente"));
        guardian.setDocumentType(DocumentType.fromDbValue(rs.getString("tipo_documento")));
        guardian.setDocumentNumber(rs.getString("numero_documento"));
        guardian.setFirstName(rs.getString("primer_nombre"));
        guardian.setMiddleName(rs.getString("segundo_nombre"));
        guardian.setFirstLastName(rs.getString("primer_apellido"));
        guardian.setSecondLastName(rs.getString("segundo_apellido"));
        guardian.setPrimaryPhone(rs.getString("telefono_principal"));
        guardian.setSecondaryPhone(rs.getString("telefono_secundario"));
        guardian.setEmail(rs.getString("correo_electronico"));
        guardian.setAddress(rs.getString("direccion"));
        guardian.setOccupation(rs.getString("ocupacion"));
        return guardian;
    }

    private void setGuardianParameters(PreparedStatement ps, Guardian guardian) throws SQLException {
        ps.setString(1, guardian.getDocumentType().getDbValue());
        ps.setString(2, guardian.getDocumentNumber());
        ps.setString(3, guardian.getFirstName());
        ps.setString(4, guardian.getMiddleName());
        ps.setString(5, guardian.getFirstLastName());
        ps.setString(6, guardian.getSecondLastName());
        ps.setString(7, guardian.getPrimaryPhone());
        ps.setString(8, guardian.getSecondaryPhone());
        ps.setString(9, guardian.getEmail());
        ps.setString(10, guardian.getAddress());
        ps.setString(11, guardian.getOccupation());
    }

    @Override
    public int create(Guardian value) {
        try (Connection connection = getConnection()) {
            return create(value, connection);
        } catch (SQLException e) {
            logError("GuardianDAOImpl.create", e);
            throw new DataAccessException("No se pudo crear el registro", e);
        }
    }

    @Override
    public int create(Guardian value, Connection connection) {
        String sql = "INSERT INTO acudientes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, " +
                "primer_apellido, segundo_apellido, telefono_principal, telefono_secundario, correo_electronico, " +
                "direccion, ocupacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setGuardianParameters(ps, value);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DataAccessException("No se obtuvo el id generado");
        } catch (SQLException e) {
            logError("GuardianDAOImpl.create", e);
            throw new DataAccessException("No se pudo crear el registro", e);
        }
    }

    @Override
    public Optional<Guardian> findById(Integer id) {
        String sql = "SELECT * FROM acudientes WHERE id_acudiente = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            logError("GuardianDAOImpl.findById", e);
            throw new DataAccessException("No se pudo realizar la consulta", e);
        }
    }

    @Override
    public Optional<Guardian> findByDocument(String documentNumber, DocumentType documentType) {
        String sql = "SELECT * FROM acudientes WHERE numero_documento = ? AND tipo_documento = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, documentNumber);
            ps.setString(2, documentType.getDbValue());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            logError("GuardianDAOImpl.findByDocument", e);
            throw new DataAccessException("No se pudo realizar la consulta", e);
        }
    }

    @Override
    public boolean existsByDocument(DocumentType documentType, String documentNumber) {
        String sql = "SELECT 1 FROM acudientes WHERE numero_documento = ? AND tipo_documento = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, documentNumber);
            ps.setString(2, documentType.getDbValue());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logError("GuardianDAOImpl.existsByDocument", e);
            throw new DataAccessException("No se pudo realizar la consulta", e);
        }
    }

    @Override
    public List<Guardian> findAll() {
        String sql = "SELECT * FROM acudientes ORDER BY primer_apellido, primer_nombre";
        List<Guardian> results = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            logError("GuardianDAOImpl.findAll", e);
            throw new DataAccessException("No se pudo realizar la consulta", e);
        }
    }

    @Override
    public List<Guardian> searchByName(String name) {
        String sql = "SELECT * FROM acudientes WHERE primer_nombre LIKE ? OR segundo_nombre LIKE ? " +
                "OR primer_apellido LIKE ? OR segundo_apellido LIKE ? ORDER BY primer_apellido, primer_nombre";
        List<Guardian> results = new ArrayList<>();
        String pattern = "%" + name + "%";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            for (int i = 1; i <= 4; i++) {
                ps.setString(i, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            logError("GuardianDAOImpl.searchByName", e);
            throw new DataAccessException("No se pudo realizar la consulta", e);
        }
    }

    @Override
    public void update(Guardian guardian) {
        String sql = "UPDATE acudientes SET tipo_documento = ?, numero_documento = ?, primer_nombre = ?, " +
                "segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, telefono_principal = ?, " +
                "telefono_secundario = ?, correo_electronico = ?, direccion = ?, ocupacion = ? " +
                "WHERE id_acudiente = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            setGuardianParameters(ps, guardian);
            ps.setInt(12, guardian.getId());

            if (ps.executeUpdate() < 1) {
                throw new DataAccessException("No se encontro o modifico el registro");
            }
        } catch (SQLException e) {
            logError("GuardianDAOImpl.update", e);
            throw new DataAccessException("No se pudo modificar el registro", e);
        }
    }
}
