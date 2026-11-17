package dao.impl;

import dao.StudentDAO;
import model.Student;
import model.enums.DocumentType;
import model.enums.Gender;
import model.enums.StudentStatus;
import util.exception.DataAccessException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static util.AppLogger.logError;
import static util.ConnectionDB.getConnection;

public class StudentDAOImpl implements StudentDAO {

    @Override
    public int create(Student student) {
        try (Connection connection = getConnection()) {
            return create(student, connection);
        } catch (SQLException e) {
            logError("StudentDAOImpl.create", e);
            throw new DataAccessException("No se pudo crear al estudiante", e);
        }
    }

    @Override
    public int create(Student student, Connection connection) {
        String sql = "INSERT INTO estudiantes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, " +
                "primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, " +
                "tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, correo_electronico, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStudentParameters(ps, student);
            ps.setString(16, student.getStatus().getDbValue());

            ps.executeUpdate();

            try (ResultSet key = ps.getGeneratedKeys()) {
                if (key.next()) {
                    return key.getInt(1);
                }
            }
            throw new DataAccessException("No se obtuvo el id del estudiante creado");
        } catch (SQLException e) {
            logError("StudentDAOImpl.create", e);
            throw new DataAccessException("No se pudo crear al estudiante", e);
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getInt("id_estudiante"));
        student.setDocumentType(DocumentType.fromDbValue(rs.getString("tipo_documento")));
        student.setDocumentNumber(rs.getString("numero_documento"));
        student.setFirstName(rs.getString("primer_nombre"));
        student.setMiddleName(rs.getString("segundo_nombre"));
        student.setFirstLastName(rs.getString("primer_apellido"));
        student.setSecondLastName(rs.getString("segundo_apellido"));
        student.setBirthDate(rs.getDate("fecha_nacimiento").toLocalDate());
        student.setGender(Gender.fromDbValue(rs.getString("genero")));
        student.setAddress(rs.getString("direccion"));
        student.setPhone(rs.getString("telefono"));
        student.setEmail(rs.getString("correo_electronico"));
        student.setBloodType(rs.getString("tipo_sangre"));
        student.setAllergies(rs.getString("alergias"));
        student.setMedicalConditions(rs.getString("condiciones_medicas"));
        student.setAdmissionDate(rs.getDate("fecha_ingreso").toLocalDate());
        student.setStatus(StudentStatus.fromDbValue(rs.getString("estado")));
        return student;
    }

    @Override
    public Optional<Student> findById(Integer id) {
        String sql = "SELECT * FROM estudiantes WHERE id_estudiante = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logError("StudentDAOImpl.findById", e);
            throw new DataAccessException("No se pudo buscar al estudiante por su id", e);
        }
    }

    @Override
    public Optional<Student> findByDocument(String documentNumber, DocumentType documentType) {
        String sql = "SELECT * FROM estudiantes WHERE numero_documento = ? AND tipo_documento = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, documentNumber);
            ps.setString(2, documentType.getDbValue());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logError("StudentDAOImpl.findByDocument", e);
            throw new DataAccessException("No se pudo buscar al estudiante por su documento", e);
        }
    }

    @Override
    public boolean existsByDocument(DocumentType documentType, String documentNumber) {
        String sql = "SELECT 1 FROM estudiantes WHERE numero_documento = ? AND tipo_documento = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, documentNumber);
            ps.setString(2, documentType.getDbValue());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logError("StudentDAOImpl.existsByDocument", e);
            throw new DataAccessException("No se pudo buscar al estudiante por su documento", e);
        }
    }

    @Override
    public List<Student> findAll() {
        String sql = "SELECT * FROM estudiantes";
        List<Student> students = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(mapRow(rs));
                }
            }
            return students;
        } catch (SQLException e) {
            logError("StudentDAOImpl.findAll", e);
            throw new DataAccessException("No se pudo buscar los estudiantes", e);
        }
    }

    @Override
    public List<Student> searchByName(String name) {
        String sql = "SELECT * FROM estudiantes " +
                "WHERE primer_nombre LIKE ? OR segundo_nombre LIKE ? " +
                "OR primer_apellido LIKE ? OR segundo_apellido LIKE ? " +
                "ORDER BY primer_apellido, segundo_apellido, primer_nombre";

        List<Student> students = new ArrayList<>();
        String pattern = "%" + name + "%";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(mapRow(rs));
                }
            }
            return students;
        } catch (SQLException e) {
            logError("StudentDAOImpl.searchByName", e);
            throw new DataAccessException("No se pudo buscar los estudiantes por su nombre", e);
        }
    }

    private void setStudentParameters(PreparedStatement ps, Student student) throws SQLException {
        ps.setString(1, student.getDocumentType().getDbValue());
        ps.setString(2, student.getDocumentNumber());
        ps.setString(3, student.getFirstName());
        ps.setString(4, student.getMiddleName());
        ps.setString(5, student.getFirstLastName());
        ps.setString(6, student.getSecondLastName());
        ps.setDate(7, Date.valueOf(student.getBirthDate()));
        ps.setString(8, student.getGender().getDbValue());
        ps.setString(9, student.getAddress());
        ps.setString(10, student.getPhone());
        ps.setString(11, student.getBloodType());
        ps.setString(12, student.getAllergies());
        ps.setString(13, student.getMedicalConditions());
        ps.setDate(14, Date.valueOf(student.getAdmissionDate()));
        ps.setString(15, student.getEmail());
    }

    @Override
    public void update(Student student) {
        String sql = "UPDATE estudiantes SET tipo_documento = ?, numero_documento = ?, primer_nombre = ?, " +
                "segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, fecha_nacimiento = ?, genero = ?, " +
                "direccion = ?, telefono = ?, tipo_sangre = ?, alergias = ?, condiciones_medicas = ?, " +
                "fecha_ingreso = ?, correo_electronico = ? WHERE id_estudiante = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            setStudentParameters(ps, student);
            ps.setInt(16, student.getId());

            if (ps.executeUpdate() < 1) {
                throw new DataAccessException("No se modifico ningun estudiante");
            }
        } catch (SQLException e) {
            logError("StudentDAOImpl.update", e);
            throw new DataAccessException("No se pudo actualizar al estudiante", e);
        }
    }

    @Override
    public void updateStatus(StudentStatus studentStatus, Integer id) {
        String sql = "UPDATE estudiantes SET estado = ? WHERE id_estudiante = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, studentStatus.getDbValue());
            ps.setInt(2, id);

            if (ps.executeUpdate() < 1) {
                throw new DataAccessException("No se pudo modificar el estado del estudiante");
            }
        } catch (SQLException e) {
            logError("StudentDAOImpl.updateStatus", e);
            throw new DataAccessException("No se pudo actualizar el estado del estudiante", e);
        }
    }
}
