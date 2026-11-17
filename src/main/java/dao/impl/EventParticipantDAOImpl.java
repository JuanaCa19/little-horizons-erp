package dao.impl;

import dao.EventParticipantDAO;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import model.EventParticipant;
import model.enums.*;

public class EventParticipantDAOImpl implements EventParticipantDAO {
    private final JdbcSupport jdbc;
    public EventParticipantDAOImpl() { jdbc=new JdbcSupport(); }
    public EventParticipantDAOImpl(Supplier<Connection> connections) { jdbc=new JdbcSupport(connections); }

    private EventParticipant mapRow(ResultSet rs) throws SQLException {
        EventParticipant value=new EventParticipant();
        value.setId(rs.getInt("id_participacion"));
        value.setEventId(rs.getInt("id_evento"));
        value.setStudentId(rs.getInt("id_estudiante"));
        value.setRole(EventRole.fromDbValue(rs.getString("rol_participacion")));
        value.setPosition(rs.getString("posicion_obtenida")==null ? null : EventPosition.fromDbValue(rs.getString("posicion_obtenida")));
        value.setNotes(rs.getString("observaciones"));
        return value;
    }

    private void bind(PreparedStatement ps,EventParticipant value) throws SQLException {
        ps.setInt(1,value.getEventId());
        ps.setInt(2,value.getStudentId());
        ps.setString(3,value.getRole().getDbValue());
        ps.setString(4,value.getPosition()==null ? null : value.getPosition().getDbValue());
        ps.setString(5,value.getNotes());
    }

    @Override public Optional<EventParticipant> findById(Integer id,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM participantes_evento WHERE id_participacion=?",ps -> ps.setInt(1,id),this::mapRow).stream().findFirst();
    }
    @Override public int create(EventParticipant value,Connection c) { return JdbcSupport.insert(c,"INSERT INTO participantes_evento (id_evento,id_estudiante,rol_participacion,posicion_obtenida,observaciones) VALUES (?,?,?,?,?)",ps -> bind(ps,value)); }
    @Override public void update(EventParticipant value,Connection c) {
        JdbcSupport.update(c,"UPDATE participantes_evento SET id_evento=?,id_estudiante=?,rol_participacion=?,posicion_obtenida=?,observaciones=? WHERE id_participacion=?",ps -> { bind(ps,value);ps.setInt(6,value.getId()); });
    }

    @Override public List<EventParticipant> findByStudentId(Integer studentId) {
        return jdbc.execute(c -> JdbcSupport.query(c,"SELECT * FROM participantes_evento WHERE id_estudiante=? ORDER BY id_evento,id_participacion",ps -> ps.setInt(1,studentId),this::mapRow));
    }
    @Override public List<EventParticipant> findByEventId(Integer eventId) {
        return jdbc.execute(c -> JdbcSupport.query(c,"SELECT * FROM participantes_evento WHERE id_evento=? ORDER BY posicion_obtenida,id_estudiante",ps -> ps.setInt(1,eventId),this::mapRow));
    }
    @Override public Optional<EventParticipant> findByPair(Integer eventId,Integer studentId,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM participantes_evento WHERE id_evento=? AND id_estudiante=?",ps -> { ps.setInt(1,eventId);ps.setInt(2,studentId); },this::mapRow).stream().findFirst();
    }

}
