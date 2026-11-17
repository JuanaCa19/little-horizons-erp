package dao.impl;

import dao.AcademicRecordDAO;
import java.sql.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import model.*;
import model.enums.*;

public class AcademicRecordDAOImpl implements AcademicRecordDAO {
    private final JdbcSupport jdbc;
    public AcademicRecordDAOImpl() { jdbc = new JdbcSupport(); }
    public AcademicRecordDAOImpl(Supplier<Connection> connections) { jdbc = new JdbcSupport(connections); }

    private AcademicRecord mapRow(ResultSet rs) throws SQLException {
        AcademicRecord value = new AcademicRecord();
        value.setId(rs.getInt("id_registro"));
        value.setStudentId(rs.getInt("id_estudiante"));
        value.setSubjectId(rs.getInt("id_materia"));
        value.setTeacherId(rs.getInt("id_profesor"));
        value.setAcademicYear(rs.getInt("anio_academico"));
        value.setPeriod(AcademicPeriod.fromDbValue(rs.getString("periodo_academico")));
        value.setGrade(rs.getBigDecimal("valor_nota"));
        value.setNotes(rs.getString("observaciones"));
        value.setRecordDate(rs.getObject("fecha_registro", LocalDate.class));
        return value;
    }

    private void bind(PreparedStatement ps, AcademicRecord value) throws SQLException {
        ps.setInt(1, value.getStudentId());
        ps.setInt(2, value.getSubjectId());
        ps.setInt(3, value.getTeacherId());
        ps.setInt(4, value.getAcademicYear());
        ps.setString(5, value.getPeriod().getDbValue());
        ps.setBigDecimal(6, value.getGrade());
        ps.setString(7, value.getNotes());
        ps.setObject(8, value.getRecordDate(), Types.DATE);
    }

    @Override public int create(AcademicRecord value, Connection c) {
        return JdbcSupport.insert(c, "INSERT INTO registros_academicos (id_estudiante,id_materia,id_profesor,anio_academico,periodo_academico,valor_nota,observaciones,fecha_registro) VALUES (?,?,?,?,?,?,?,?)", ps -> bind(ps, value));
    }

    @Override public Optional<AcademicRecord> findById(Integer id, Connection c) {
        return JdbcSupport.query(c, "SELECT * FROM registros_academicos WHERE id_registro=?", ps -> ps.setInt(1, id), this::mapRow).stream().findFirst();
    }

    @Override public Optional<AcademicRecord> findByKey(Integer studentId, Integer subjectId, int year, AcademicPeriod period, Connection c) {
        return JdbcSupport.query(c, "SELECT * FROM registros_academicos WHERE id_estudiante=? AND id_materia=? AND anio_academico=? AND periodo_academico=?",
                ps -> { ps.setInt(1,studentId); ps.setInt(2,subjectId); ps.setInt(3,year); ps.setString(4,period.getDbValue()); }, this::mapRow).stream().findFirst();
    }
    @Override public List<AcademicRecord> findBySubjectAndPeriod(Integer subjectId, int year, AcademicPeriod period) {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM registros_academicos WHERE id_materia=? AND anio_academico=? AND periodo_academico=? ORDER BY id_estudiante",
                ps -> { ps.setInt(1,subjectId); ps.setInt(2,year); ps.setString(3,period.getDbValue()); }, this::mapRow));
    }


    @Override public List<AcademicRecord> findByStudentId(Integer studentId) {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM registros_academicos WHERE id_estudiante=? ORDER BY anio_academico,id_materia,periodo_academico", ps -> ps.setInt(1,studentId), this::mapRow));
    }


    @Override public void update(AcademicRecord value, Connection c) {
        JdbcSupport.update(c,"UPDATE registros_academicos SET id_estudiante=?,id_materia=?,id_profesor=?,anio_academico=?,periodo_academico=?,valor_nota=?,observaciones=?,fecha_registro=? WHERE id_registro=?",ps -> { bind(ps,value); ps.setInt(9,value.getId()); });
    }
}
