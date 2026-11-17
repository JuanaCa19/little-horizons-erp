package dao.impl;

import dao.TeacherDAO;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import model.*;
import model.enums.*;
import util.exception.DataAccessException;

public class TeacherDAOImpl implements TeacherDAO {
    private final JdbcSupport jdbc;
    public TeacherDAOImpl() { jdbc = new JdbcSupport(); }
    public TeacherDAOImpl(Supplier<Connection> connections) { jdbc = new JdbcSupport(connections); }

    private Teacher mapRow(ResultSet rs) throws SQLException {
        Teacher value = new Teacher();
        value.setId(rs.getInt("id_profesor"));
        value.setDocumentType(DocumentType.fromDbValue(rs.getString("tipo_documento")));
        value.setDocumentNumber(rs.getString("numero_documento"));
        value.setFirstName(rs.getString("primer_nombre"));
        value.setMiddleName(rs.getString("segundo_nombre"));
        value.setFirstLastName(rs.getString("primer_apellido"));
        value.setSecondLastName(rs.getString("segundo_apellido"));
        value.setEmail(rs.getString("correo_electronico"));
        value.setPhone(rs.getString("telefono"));
        value.setSpecialization(rs.getString("especializacion"));
        value.setHiringDate(rs.getObject("fecha_contratacion", LocalDate.class));
        value.setStatus(TeacherStatus.fromDbValue(rs.getString("estado")));
        return value;
    }

    @Override public Optional<Teacher> findById(Integer id) {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM profesores WHERE id_profesor=?",
                ps -> ps.setInt(1, id), this::mapRow).stream().findFirst());
    }

    private void bind(PreparedStatement ps, Teacher value) throws SQLException {
        ps.setString(1, value.getDocumentType().getDbValue());
        ps.setString(2, value.getDocumentNumber());
        ps.setString(3, value.getFirstName());
        ps.setString(4, value.getMiddleName());
        ps.setString(5, value.getFirstLastName());
        ps.setString(6, value.getSecondLastName());
        ps.setString(7, value.getEmail());
        ps.setString(8, value.getPhone());
        ps.setString(9, value.getSpecialization());
        ps.setObject(10, value.getHiringDate(), Types.DATE);
    }
    @Override public int create(Teacher value) { return jdbc.execute(c -> JdbcSupport.insert(c, "INSERT INTO profesores (tipo_documento,numero_documento,primer_nombre,segundo_nombre,primer_apellido,segundo_apellido,correo_electronico,telefono,especializacion,fecha_contratacion,estado) VALUES (?,?,?,?,?,?,?,?,?,?,?)", ps -> { bind(ps, value); ps.setString(11, value.getStatus().getDbValue()); })); }

    @Override public Optional<Teacher> findById(Integer id, Connection c) {
        return JdbcSupport.query(c, "SELECT * FROM profesores WHERE id_profesor=?", ps -> ps.setInt(1, id), this::mapRow).stream().findFirst();
    }
    @Override public Optional<Teacher> findByDocument(String number, DocumentType type) {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM profesores WHERE numero_documento=? AND tipo_documento=?",
                ps -> { ps.setString(1, number); ps.setString(2, type.getDbValue()); }, this::mapRow).stream().findFirst());
    }
    @Override public Optional<Teacher> findByEmail(String email) {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM profesores WHERE correo_electronico=?",
                ps -> ps.setString(1, email), this::mapRow).stream().findFirst());
    }
    @Override public List<Teacher> findAll() {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM profesores ORDER BY primer_apellido,primer_nombre", ps -> {}, this::mapRow));
    }
    @Override public List<Teacher> searchByName(String name) {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM profesores WHERE primer_nombre LIKE ? OR segundo_nombre LIKE ? OR primer_apellido LIKE ? OR segundo_apellido LIKE ? ORDER BY primer_apellido,primer_nombre",
                ps -> { for(int i=1;i<=4;i++) ps.setString(i, "%"+name+"%"); }, this::mapRow));
    }
    @Override public void update(Teacher value) {
        jdbc.execute(c -> {
            JdbcSupport.update(c, "UPDATE profesores SET tipo_documento=?,numero_documento=?,primer_nombre=?,segundo_nombre=?,primer_apellido=?,segundo_apellido=?,correo_electronico=?,telefono=?,especializacion=?,fecha_contratacion=? WHERE id_profesor=?",
                    ps -> { bind(ps, value); ps.setInt(11, value.getId()); });
            return null;
        });
    }
    @Override public void updateStatus(Integer id, TeacherStatus status) {
        jdbc.execute(c -> {
            JdbcSupport.update(c, "UPDATE profesores SET estado=? WHERE id_profesor=?", ps -> { ps.setString(1, status.getDbValue()); ps.setInt(2, id); });
            return null;
        });
    }
    @Override public void lock(Integer id, Connection c) {
        if(JdbcSupport.query(c, "SELECT id_profesor FROM profesores WHERE id_profesor=? FOR UPDATE", ps -> ps.setInt(1,id), rs -> rs.getInt(1)).isEmpty())
            throw new DataAccessException("Profesor no encontrado.");
    }

}
