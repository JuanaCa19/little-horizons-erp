package dao.impl;

import dao.EventDAO;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import model.Event;
import model.enums.*;

public class EventDAOImpl implements EventDAO {
    private final JdbcSupport jdbc;
    public EventDAOImpl() { jdbc=new JdbcSupport(); }
    public EventDAOImpl(Supplier<Connection> connections) { jdbc=new JdbcSupport(connections); }

    private Event mapRow(ResultSet rs) throws SQLException {
        Event value=new Event();
        value.setId(rs.getInt("id_evento"));
        value.setName(rs.getString("nombre_evento"));
        value.setDescription(rs.getString("descripcion"));
        value.setType(EventType.fromDbValue(rs.getString("tipo_evento")));
        value.setDate(rs.getObject("fecha_evento",LocalDate.class));
        value.setLocation(rs.getString("lugar"));
        value.setCompetition(rs.getBoolean("tiene_competencia"));
        value.setOrganizerId(rs.getObject("id_organizador",Integer.class));
        value.setActive(rs.getBoolean("esta_activo"));
        return value;
    }

    private void bind(PreparedStatement ps,Event value) throws SQLException {
        ps.setString(1,value.getName());
        ps.setString(2,value.getDescription());
        ps.setString(3,value.getType().getDbValue());
        ps.setObject(4,value.getDate(),Types.DATE);
        ps.setString(5,value.getLocation());
        ps.setBoolean(6,value.isCompetition());
        ps.setObject(7,value.getOrganizerId(),Types.INTEGER);
        ps.setBoolean(8,value.isActive());
    }

    @Override public Optional<Event> findById(Integer id,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM eventos WHERE id_evento=? FOR UPDATE",ps -> ps.setInt(1,id),this::mapRow).stream().findFirst();
    }
    @Override public int create(Event value) { return jdbc.execute(c -> create(value,c)); }
    @Override public int create(Event value,Connection c) { return JdbcSupport.insert(c,"INSERT INTO eventos (nombre_evento,descripcion,tipo_evento,fecha_evento,lugar,tiene_competencia,id_organizador,esta_activo) VALUES (?,?,?,?,?,?,?,?)",ps -> bind(ps,value)); }
    @Override public Optional<Event> findById(Integer id) {
        return jdbc.execute(c -> JdbcSupport.query(c,"SELECT * FROM eventos WHERE id_evento=?",ps -> ps.setInt(1,id),this::mapRow).stream().findFirst());
    }
    @Override public void update(Event value,Connection c) {
        JdbcSupport.update(c,"UPDATE eventos SET nombre_evento=?,descripcion=?,tipo_evento=?,fecha_evento=?,lugar=?,tiene_competencia=?,id_organizador=?,esta_activo=? WHERE id_evento=?",ps -> { bind(ps,value);ps.setInt(9,value.getId()); });
    }

    @Override public List<Event> findByDates(LocalDate from,LocalDate to) {
        return jdbc.execute(c -> JdbcSupport.query(c,"SELECT * FROM eventos WHERE fecha_evento BETWEEN ? AND ? ORDER BY fecha_evento,nombre_evento",ps -> { ps.setObject(1,from,Types.DATE);ps.setObject(2,to,Types.DATE); },this::mapRow));
    }
    @Override public boolean hasResults(Integer eventId,Connection c) {
        return !JdbcSupport.query(c,"SELECT id_participacion FROM participantes_evento WHERE id_evento=? AND posicion_obtenida IS NOT NULL LIMIT 1",ps -> ps.setInt(1,eventId),rs -> rs.getInt(1)).isEmpty();
    }
    @Override public boolean hasCompetitors(Integer eventId,Connection c) {
        return !JdbcSupport.query(c,"SELECT id_participacion FROM participantes_evento WHERE id_evento=? AND rol_participacion='Competidor' LIMIT 1",ps -> ps.setInt(1,eventId),rs -> rs.getInt(1)).isEmpty();
    }

}
