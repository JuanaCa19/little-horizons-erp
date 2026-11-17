package dao.impl;

import dao.ClassScheduleDAO;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import model.*;
import model.enums.*;
import util.exception.DataAccessException;

public class ClassScheduleDAOImpl implements ClassScheduleDAO {
    private final JdbcSupport jdbc;
    public ClassScheduleDAOImpl() { jdbc = new JdbcSupport(); }
    public ClassScheduleDAOImpl(Supplier<Connection> connections) { jdbc = new JdbcSupport(connections); }

    private ClassSchedule mapRow(ResultSet rs) throws SQLException {
        ClassSchedule value = new ClassSchedule();
        value.setId(rs.getInt("id_horario"));
        value.setAssignmentId(rs.getInt("id_asignacion"));
        value.setWeekDay(WeekDay.fromDbValue(rs.getString("dia_semana")));
        value.setStartTime(rs.getObject("hora_inicio", LocalTime.class));
        value.setEndTime(rs.getObject("hora_fin", LocalTime.class));
        return value;
    }

    @Override public Optional<ClassSchedule> findById(Integer id) {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM horarios_asignacion WHERE id_horario=?",
                ps -> ps.setInt(1, id), this::mapRow).stream().findFirst());
    }

    private void bind(PreparedStatement ps, ClassSchedule value) throws SQLException {
        ps.setInt(1, value.getAssignmentId());
        ps.setString(2, value.getWeekDay().getDbValue());
        ps.setObject(3, value.getStartTime(), Types.TIME);
        ps.setObject(4, value.getEndTime(), Types.TIME);
    }
    @Override public int create(ClassSchedule value, Connection c) { return JdbcSupport.insert(c, "INSERT INTO horarios_asignacion (id_asignacion,dia_semana,hora_inicio,hora_fin) VALUES (?,?,?,?)", ps -> { bind(ps, value); }); }

    @Override public List<ClassSchedule> findByAssignmentId(Integer assignmentId) {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM horarios_asignacion WHERE id_asignacion=? ORDER BY dia_semana,hora_inicio", ps -> ps.setInt(1,assignmentId), this::mapRow));
    }
    @Override public List<ClassSchedule> findByTeacherAndYear(Integer teacherId, int year) {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT h.* FROM horarios_asignacion h JOIN asignaciones_profesor a ON a.id_asignacion=h.id_asignacion WHERE a.id_profesor=? AND a.anio_academico=? ORDER BY h.dia_semana,h.hora_inicio",
                ps -> { ps.setInt(1,teacherId); ps.setInt(2,year); }, this::mapRow));
    }
    @Override public boolean hasConflict(TeacherAssignment assignment, Integer gradeId, ClassSchedule schedule, Connection c) {
        String sql = "SELECT h.id_horario FROM horarios_asignacion h JOIN asignaciones_profesor a ON a.id_asignacion=h.id_asignacion JOIN materias m ON m.id_materia=a.id_materia "
                + "WHERE a.anio_academico=? AND h.dia_semana=? AND h.hora_inicio < ? AND h.hora_fin > ? AND (a.id_profesor=? OR (m.id_grado=? AND a.seccion=?)) FOR UPDATE";
        return !JdbcSupport.query(c, sql, ps -> {
            ps.setInt(1,assignment.getAcademicYear()); ps.setString(2,schedule.getWeekDay().getDbValue());
            ps.setObject(3,schedule.getEndTime(),Types.TIME); ps.setObject(4,schedule.getStartTime(),Types.TIME);
            ps.setInt(5,assignment.getTeacherId()); ps.setInt(6,gradeId); ps.setString(7,assignment.getSection());
        }, rs -> rs.getInt(1)).isEmpty();
    }
    @Override public void lockGrade(Integer gradeId, Connection c) {
        if(JdbcSupport.query(c, "SELECT id_grado FROM grados WHERE id_grado=? FOR UPDATE", ps -> ps.setInt(1,gradeId), rs -> rs.getInt(1)).isEmpty())
            throw new DataAccessException("Grado no encontrado.");
    }
    @Override public void delete(Integer id) {
        jdbc.execute(c -> { JdbcSupport.update(c, "DELETE FROM horarios_asignacion WHERE id_horario=?", ps -> ps.setInt(1,id)); return null; });
    }

}
