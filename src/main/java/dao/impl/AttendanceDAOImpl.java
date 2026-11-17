package dao.impl;

import dao.AttendanceDAO;
import java.sql.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import model.*;
import model.enums.*;

public class AttendanceDAOImpl implements AttendanceDAO {
    private final JdbcSupport jdbc;
    public AttendanceDAOImpl() { jdbc = new JdbcSupport(); }
    public AttendanceDAOImpl(Supplier<Connection> connections) { jdbc = new JdbcSupport(connections); }

    private Attendance mapRow(ResultSet rs) throws SQLException {
        Attendance value = new Attendance();
        value.setId(rs.getInt("id_asistencia"));
        value.setStudentId(rs.getInt("id_estudiante"));
        value.setSubjectId(rs.getInt("id_materia"));
        value.setDate(rs.getObject("fecha_asistencia", LocalDate.class));
        value.setStatus(AttendanceStatus.fromDbValue(rs.getString("estado_asistencia")));
        value.setNotes(rs.getString("observaciones"));
        value.setTeacherId(rs.getObject("registrado_por", Integer.class));
        return value;
    }

    private void bind(PreparedStatement ps, Attendance value) throws SQLException {
        ps.setInt(1, value.getStudentId());
        ps.setInt(2, value.getSubjectId());
        ps.setObject(3, value.getDate(), Types.DATE);
        ps.setString(4, value.getStatus().getDbValue());
        ps.setString(5, value.getNotes());
        ps.setInt(6, value.getTeacherId());
    }

    @Override public int create(Attendance value, Connection c) {
        return JdbcSupport.insert(c, "INSERT INTO asistencias (id_estudiante,id_materia,fecha_asistencia,estado_asistencia,observaciones,registrado_por) VALUES (?,?,?,?,?,?)", ps -> bind(ps, value));
    }

    @Override public Optional<Attendance> findById(Integer id, Connection c) {
        return JdbcSupport.query(c, "SELECT * FROM asistencias WHERE id_asistencia=?", ps -> ps.setInt(1, id), this::mapRow).stream().findFirst();
    }

    @Override public Optional<Attendance> findByKey(Integer studentId, Integer subjectId, LocalDate date, Connection c) {
        return JdbcSupport.query(c, "SELECT * FROM asistencias WHERE id_estudiante=? AND id_materia=? AND fecha_asistencia=?",
                ps -> { ps.setInt(1,studentId); ps.setInt(2,subjectId); ps.setObject(3,date,Types.DATE); }, this::mapRow).stream().findFirst();
    }
    @Override public List<Attendance> findByStudentAndDates(Integer studentId, LocalDate from, LocalDate to) {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM asistencias WHERE id_estudiante=? AND fecha_asistencia BETWEEN ? AND ? ORDER BY fecha_asistencia,id_materia",
                ps -> { ps.setInt(1,studentId); ps.setObject(2,from,Types.DATE); ps.setObject(3,to,Types.DATE); }, this::mapRow));
    }
    @Override public List<Attendance> findBySubjectAndDate(Integer subjectId, LocalDate date) {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM asistencias WHERE id_materia=? AND fecha_asistencia=? ORDER BY id_estudiante",
                ps -> { ps.setInt(1,subjectId); ps.setObject(2,date,Types.DATE); }, this::mapRow));
    }
    @Override public List<AbsenceAlert> findAbsenceAlerts(LocalDate from, LocalDate to, int threshold) {
        String sql = "SELECT a.id_estudiante,CONCAT_WS(' ',e.primer_nombre,e.segundo_nombre,e.primer_apellido,e.segundo_apellido) nombre,COUNT(DISTINCT a.fecha_asistencia) ausencias "
                + "FROM asistencias a JOIN estudiantes e ON e.id_estudiante=a.id_estudiante WHERE a.estado_asistencia='Ausente' AND a.fecha_asistencia BETWEEN ? AND ? "
                + "GROUP BY a.id_estudiante,e.primer_nombre,e.segundo_nombre,e.primer_apellido,e.segundo_apellido HAVING COUNT(DISTINCT a.fecha_asistencia)>? ORDER BY ausencias DESC,a.id_estudiante";
        return jdbc.execute(c -> JdbcSupport.query(c,sql,ps -> { ps.setObject(1,from,Types.DATE); ps.setObject(2,to,Types.DATE); ps.setInt(3,threshold); },
                rs -> new AbsenceAlert(rs.getInt("id_estudiante"),rs.getString("nombre"),rs.getInt("ausencias"))));
    }


    @Override public void update(Attendance value, Connection c) {
        JdbcSupport.update(c,"UPDATE asistencias SET id_estudiante=?,id_materia=?,fecha_asistencia=?,estado_asistencia=?,observaciones=?,registrado_por=? WHERE id_asistencia=?",ps -> { bind(ps,value); ps.setInt(7,value.getId()); });
    }
}
