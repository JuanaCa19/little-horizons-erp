package controller;

import java.time.LocalDate;
import java.util.*;
import model.*;
import service.EventService;

public class EventController {
    private final EventService service;
    public EventController() { this(new EventService()); }
    public EventController(EventService service) { this.service=Objects.requireNonNull(service); }

    public int create(Event value) { return service.create(value); }

    public Event getById(Integer id) { return service.getById(id); }

    public List<Event> findByDates(LocalDate from,LocalDate to) { return service.findByDates(from,to); }

    public void update(Event value) { service.update(value); }

    public int registerParticipant(EventParticipant value) { return service.registerParticipant(value); }

    public EventParticipant getParticipant(Integer id) { return service.getParticipant(id); }

    public void updateParticipant(EventParticipant value) { service.updateParticipant(value); }

    public List<EventParticipant> findParticipantsByEvent(Integer eventId) { return service.findParticipantsByEvent(eventId); }

    public List<EventParticipant> findParticipantsByStudent(Integer studentId) { return service.findParticipantsByStudent(studentId); }
}
