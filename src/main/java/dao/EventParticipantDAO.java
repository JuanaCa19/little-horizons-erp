package dao;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import model.EventParticipant;

public interface EventParticipantDAO {
    Optional<EventParticipant> findById(Integer id,Connection connection);
    List<EventParticipant> findByStudentId(Integer studentId);
    List<EventParticipant> findByEventId(Integer eventId);
    Optional<EventParticipant> findByPair(Integer eventId,Integer studentId,Connection connection);
    void update(EventParticipant value,Connection connection);
    int create(EventParticipant value,Connection connection);
}
