package dao.impl;

import dao.SubjectEnrollmentDAO;
import java.sql.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import model.*;
import model.enums.*;

public class SubjectEnrollmentDAOImpl implements SubjectEnrollmentDAO {
    private final JdbcSupport jdbc;
    public SubjectEnrollmentDAOImpl() { jdbc = new JdbcSupport(); }
    public SubjectEnrollmentDAOImpl(Supplier<Connection> connections) { jdbc = new JdbcSupport(connections); }

    private SubjectEnrollment mapRow(ResultSet rs) throws SQLException {
        SubjectEnrollment value = new SubjectEnrollment();
        value.setId(rs.getInt("id_inscripcion"));
        value.setStudentId(rs.getInt("id_estudiante"));
        value.setSubjectId(rs.getInt("id_materia"));
        value.setAcademicYear(rs.getInt("anio_academico"));
        value.setPeriod(AcademicPeriod.fromDbValue(rs.getString("periodo_academico")));
        value.setSection(rs.getString("seccion"));
        value.setEnrollmentDate(rs.getObject("fecha_inscripcion", LocalDate.class));
        value.setStatus(EnrollmentStatus.fromDbValue(rs.getString("estado")));
        return value;
    }

    private void bind(PreparedStatement ps, SubjectEnrollment value) throws SQLException {
        ps.setInt(1, value.getStudentId());
        ps.setInt(2, value.getSubjectId());
        ps.setInt(3, value.getAcademicYear());
        ps.setString(4, value.getPeriod().getDbValue());
        ps.setString(5, value.getSection());
        ps.setObject(6, value.getEnrollmentDate(), Types.DATE);
        ps.setString(7, value.getStatus().getDbValue());
    }

    @Override public int create(SubjectEnrollment value, Connection c) {
        return JdbcSupport.insert(c, "INSERT INTO inscripciones_materia (id_estudiante,id_materia,anio_academico,periodo_academico,seccion,fecha_inscripcion,estado) VALUES (?,?,?,?,?,?,?)", ps -> bind(ps, value));
    }

    @Override public Optional<SubjectEnrollment> findById(Integer id, Connection c) {
        return JdbcSupport.query(c, "SELECT * FROM inscripciones_materia WHERE id_inscripcion=?", ps -> ps.setInt(1, id), this::mapRow).stream().findFirst();
    }

    @Override public Optional<SubjectEnrollment> findByKey(Integer studentId, Integer subjectId, int year, AcademicPeriod period, Connection c) {
        return JdbcSupport.query(c, "SELECT * FROM inscripciones_materia WHERE id_estudiante=? AND id_materia=? AND anio_academico=? AND periodo_academico=?",
                ps -> { ps.setInt(1,studentId); ps.setInt(2,subjectId); ps.setInt(3,year); ps.setString(4,period.getDbValue()); }, this::mapRow).stream().findFirst();
    }
    @Override public List<SubjectEnrollment> findBySubjectAndPeriod(Integer subjectId, int year, AcademicPeriod period) {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM inscripciones_materia WHERE id_materia=? AND anio_academico=? AND periodo_academico=? ORDER BY id_estudiante",
                ps -> { ps.setInt(1,subjectId); ps.setInt(2,year); ps.setString(3,period.getDbValue()); }, this::mapRow));
    }


    @Override public List<SubjectEnrollment> findByStudentAndYear(Integer studentId, int year) {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM inscripciones_materia WHERE id_estudiante=? AND anio_academico=? ORDER BY id_materia,periodo_academico",
                ps -> { ps.setInt(1,studentId); ps.setInt(2,year); }, this::mapRow));
    }
    @Override public void updateStatus(Integer id, EnrollmentStatus status, Connection c) {
        JdbcSupport.update(c, "UPDATE inscripciones_materia SET estado=? WHERE id_inscripcion=?",
                ps -> { ps.setString(1,status.getDbValue()); ps.setInt(2,id); });
    }

}
