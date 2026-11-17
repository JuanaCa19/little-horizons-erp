package dao.impl;

import dao.TeacherAssignmentDAO;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import model.*;
import model.enums.*;
import util.exception.DataAccessException;

public class TeacherAssignmentDAOImpl implements TeacherAssignmentDAO {
    private final JdbcSupport jdbc;
    public TeacherAssignmentDAOImpl() { jdbc = new JdbcSupport(); }
    public TeacherAssignmentDAOImpl(Supplier<Connection> connections) { jdbc = new JdbcSupport(connections); }

    private TeacherAssignment mapRow(ResultSet rs) throws SQLException {
        TeacherAssignment value = new TeacherAssignment();
        value.setId(rs.getInt("id_asignacion"));
        value.setTeacherId(rs.getInt("id_profesor"));
        value.setSubjectId(rs.getInt("id_materia"));
        value.setAcademicYear(rs.getInt("anio_academico"));
        value.setSection(rs.getString("seccion"));
        return value;
    }

    @Override public Optional<TeacherAssignment> findById(Integer id) {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM asignaciones_profesor WHERE id_asignacion=?",
                ps -> ps.setInt(1, id), this::mapRow).stream().findFirst());
    }

    private void bind(PreparedStatement ps, TeacherAssignment value) throws SQLException {
        ps.setInt(1, value.getTeacherId());
        ps.setInt(2, value.getSubjectId());
        ps.setInt(3, value.getAcademicYear());
        ps.setString(4, value.getSection());
    }
    @Override public int create(TeacherAssignment value, Connection c) { return JdbcSupport.insert(c, "INSERT INTO asignaciones_profesor (id_profesor,id_materia,anio_academico,seccion) VALUES (?,?,?,?)", ps -> { bind(ps, value); }); }

    @Override public boolean exists(Integer teacherId, Integer subjectId, int year, String section, Connection c) {
        return !JdbcSupport.query(c, "SELECT id_asignacion FROM asignaciones_profesor WHERE id_profesor=? AND id_materia=? AND anio_academico=? AND seccion=?",
                ps -> { ps.setInt(1,teacherId); ps.setInt(2,subjectId); ps.setInt(3,year); ps.setString(4,section); }, rs -> rs.getInt(1)).isEmpty();
    }
    @Override public List<TeacherAssignment> findByTeacherAndYear(Integer teacherId, int year) {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM asignaciones_profesor WHERE id_profesor=? AND anio_academico=? ORDER BY id_materia,seccion",
                ps -> { ps.setInt(1,teacherId); ps.setInt(2,year); }, this::mapRow));
    }

}
