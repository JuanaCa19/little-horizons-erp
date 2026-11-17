package dao.impl;

import dao.ClubDAO;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import model.Club;
import model.enums.*;

public class ClubDAOImpl implements ClubDAO {
    private final JdbcSupport jdbc;
    public ClubDAOImpl() { jdbc=new JdbcSupport(); }
    public ClubDAOImpl(Supplier<Connection> connections) { jdbc=new JdbcSupport(connections); }

    private Club mapRow(ResultSet rs) throws SQLException {
        Club value=new Club();
        value.setId(rs.getInt("id_club"));
        value.setName(rs.getString("nombre_club"));
        value.setDescription(rs.getString("descripcion"));
        value.setSupervisorId(rs.getObject("id_profesor_supervisor",Integer.class));
        value.setCapacity(rs.getInt("capacidad_maxima"));
        value.setMeetingSchedule(rs.getString("horario_reunion"));
        value.setActive(rs.getBoolean("esta_activo"));
        return value;
    }

    private void bind(PreparedStatement ps,Club value) throws SQLException {
        ps.setString(1,value.getName());
        ps.setString(2,value.getDescription());
        ps.setObject(3,value.getSupervisorId(),Types.INTEGER);
        ps.setInt(4,value.getCapacity());
        ps.setString(5,value.getMeetingSchedule());
        ps.setBoolean(6,value.isActive());
    }

    @Override public Optional<Club> findById(Integer id,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM clubes WHERE id_club=? FOR UPDATE",ps -> ps.setInt(1,id),this::mapRow).stream().findFirst();
    }
    @Override public int create(Club value) { return jdbc.execute(c -> create(value,c)); }
    @Override public int create(Club value,Connection c) { return JdbcSupport.insert(c,"INSERT INTO clubes (nombre_club,descripcion,id_profesor_supervisor,capacidad_maxima,horario_reunion,esta_activo) VALUES (?,?,?,?,?,?)",ps -> bind(ps,value)); }
    @Override public Optional<Club> findById(Integer id) {
        return jdbc.execute(c -> JdbcSupport.query(c,"SELECT * FROM clubes WHERE id_club=?",ps -> ps.setInt(1,id),this::mapRow).stream().findFirst());
    }
    @Override public void update(Club value,Connection c) {
        JdbcSupport.update(c,"UPDATE clubes SET nombre_club=?,descripcion=?,id_profesor_supervisor=?,capacidad_maxima=?,horario_reunion=?,esta_activo=? WHERE id_club=?",ps -> { bind(ps,value);ps.setInt(7,value.getId()); });
    }

    @Override public Optional<Club> findByName(String name) {
        return jdbc.execute(c -> JdbcSupport.query(c,"SELECT * FROM clubes WHERE nombre_club=?",ps -> ps.setString(1,name),this::mapRow).stream().findFirst());
    }
    @Override public List<Club> findAll() {
        return jdbc.execute(c -> JdbcSupport.query(c,"SELECT * FROM clubes ORDER BY nombre_club",ps -> {},this::mapRow));
    }

}
