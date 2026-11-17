package dao.impl;

import dao.SubjectDAO;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import model.*;
import model.enums.*;
import util.exception.DataAccessException;

public class SubjectDAOImpl implements SubjectDAO {
    private final JdbcSupport jdbc;
    public SubjectDAOImpl() { jdbc = new JdbcSupport(); }
    public SubjectDAOImpl(Supplier<Connection> connections) { jdbc = new JdbcSupport(connections); }

    private Subject mapRow(ResultSet rs) throws SQLException {
        Subject value = new Subject();
        value.setId(rs.getInt("id_materia"));
        value.setCode(rs.getString("codigo_materia"));
        value.setName(rs.getString("nombre_materia"));
        value.setDescription(rs.getString("descripcion"));
        value.setGradeId(rs.getInt("id_grado"));
        value.setWeeklyHours(rs.getInt("horas_semanales"));
        value.setActive(rs.getBoolean("esta_activa"));
        return value;
    }

    @Override public Optional<Subject> findById(Integer id) {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM materias WHERE id_materia=?",
                ps -> ps.setInt(1, id), this::mapRow).stream().findFirst());
    }

    @Override public List<Subject> findAll() {
        return jdbc.execute(c -> JdbcSupport.query(c, "SELECT * FROM materias ORDER BY id_grado,nombre_materia", ps -> {}, this::mapRow));
    }


    private void bind(PreparedStatement ps, Subject value) throws SQLException {
        ps.setString(1,value.getCode()); ps.setString(2,value.getName()); ps.setString(3,value.getDescription());
        ps.setInt(4,value.getGradeId()); ps.setInt(5,value.getWeeklyHours()); ps.setBoolean(6,value.isActive());
    }
    @Override public int create(Subject value) {
        return jdbc.execute(c -> JdbcSupport.insert(c,"INSERT INTO materias (codigo_materia,nombre_materia,descripcion,id_grado,horas_semanales,esta_activa) VALUES (?,?,?,?,?,?)",ps -> bind(ps,value)));
    }
    @Override public Optional<Subject> findByCode(String code) {
        return jdbc.execute(c -> JdbcSupport.query(c,"SELECT * FROM materias WHERE codigo_materia=?",ps -> ps.setString(1,code),this::mapRow).stream().findFirst());
    }
    @Override public Optional<Subject> findByNameAndGrade(String name,Integer gradeId) {
        return jdbc.execute(c -> JdbcSupport.query(c,"SELECT * FROM materias WHERE nombre_materia=? AND id_grado=?",ps -> { ps.setString(1,name);ps.setInt(2,gradeId); },this::mapRow).stream().findFirst());
    }
    @Override public boolean hasUsage(Integer subjectId) {
        String sql="SELECT EXISTS(SELECT 1 FROM asignaciones_profesor WHERE id_materia=?) OR EXISTS(SELECT 1 FROM inscripciones_materia WHERE id_materia=?) "
                + "OR EXISTS(SELECT 1 FROM registros_academicos WHERE id_materia=?) OR EXISTS(SELECT 1 FROM asistencias WHERE id_materia=?)";
        return jdbc.execute(c -> JdbcSupport.query(c,sql,ps -> { for(int i=1;i<=4;i++) ps.setInt(i,subjectId); },rs -> rs.getBoolean(1)).getFirst());
    }
    @Override public void update(Subject value) {
        jdbc.execute(c -> { JdbcSupport.update(c,"UPDATE materias SET codigo_materia=?,nombre_materia=?,descripcion=?,id_grado=?,horas_semanales=?,esta_activa=? WHERE id_materia=?",
                ps -> { bind(ps,value);ps.setInt(7,value.getId()); });return null; });
    }
}
