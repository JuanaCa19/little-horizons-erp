package dao;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import model.Event;

public interface EventDAO {
    Optional<Event> findById(Integer id,Connection connection);
    int create(Event value);
    int create(Event value,Connection connection);
    Optional<Event> findById(Integer id);
    List<Event> findByDates(LocalDate from,LocalDate to);
    boolean hasResults(Integer eventId,Connection connection);
    boolean hasCompetitors(Integer eventId,Connection connection);
    void update(Event value,Connection connection);
}
