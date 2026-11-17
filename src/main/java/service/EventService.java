package service;

import dao.*;
import dao.impl.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import model.*;
import model.enums.*;
import util.ConnectionDB;
import util.exception.ValidationException;
import static service.ServiceValidation.*;

public class EventService {
    private final EventDAO events;
    private final EventParticipantDAO participants;
    private final ExtracurricularContextDAO context;
    private final TransactionManager transactions;
    public EventService() { this(new EventDAOImpl(),new EventParticipantDAOImpl(),new ExtracurricularContextDAOImpl(),ConnectionDB::getConnection); }
    public EventService(EventDAO events,EventParticipantDAO participants,ExtracurricularContextDAO context,Supplier<Connection> connections) {
        this.events=Objects.requireNonNull(events);this.participants=Objects.requireNonNull(participants);
        this.context=Objects.requireNonNull(context);this.transactions=new TransactionManager(connections);
    }
    private void validate(Event value) {
        require(value!=null,"El evento es obligatorio.");value.setName(text(value.getName(),"El nombre"));
        require(value.getName().length()<=150,"El nombre admite hasta 150 caracteres.");
        require(value.getType()!=null,"El tipo de evento es obligatorio.");require(value.getDate()!=null,"La fecha es obligatoria.");
        require(value.getLocation()==null || value.getLocation().length()<=150,"El lugar admite hasta 150 caracteres.");
        if(value.getOrganizerId()!=null) id(value.getOrganizerId());
    }
    public int create(Event value) {
        require(value!=null && value.getId()==null,"El nuevo evento no debe tener identificador.");validate(value);
        int id=transactions.execute(c -> {
            if(value.getOrganizerId()!=null) context.requireActiveTeacher(value.getOrganizerId(),c);
            return events.create(value,c);
        });value.setId(id);return id;
    }
    public Event getById(Integer eventId) { id(eventId);return events.findById(eventId).orElseThrow(() -> new ValidationException("Evento no encontrado.")); }
    public List<Event> findByDates(LocalDate from,LocalDate to) {
        require(from!=null && to!=null && !to.isBefore(from),"El rango de fechas no es valido.");return events.findByDates(from,to);
    }
    public void update(Event value) {
        validate(value);id(value.getId());
        transactions.execute(c -> {
            Event existing=events.findById(value.getId(),c).orElseThrow(() -> new ValidationException("Evento no encontrado."));
            boolean results=events.hasResults(value.getId(),c);
            require(value.isCompetition() || !events.hasCompetitors(value.getId(),c) && !results,"El evento ya tiene competidores o resultados; debe conservar la competencia.");
            require(!results || Objects.equals(existing.getDate(),value.getDate()),"No se puede cambiar la fecha de un evento con resultados.");
            if(value.getOrganizerId()!=null && !Objects.equals(existing.getOrganizerId(),value.getOrganizerId())) context.requireActiveTeacher(value.getOrganizerId(),c);
            events.update(value,c);return null;
        });
    }
    private void validateParticipation(EventParticipant value,Event event) {
        require(value.getRole()!=null,"El rol de participacion es obligatorio.");
        require(value.getRole()!=EventRole.COMPETITOR || event.isCompetition(),"Solo los eventos con competencia admiten competidores.");
        if(value.getPosition()!=null) {
            require(event.isCompetition(),"Solo los eventos con competencia admiten posiciones.");
            require(!event.getDate().isAfter(LocalDate.now()),"No se pueden registrar resultados de un evento futuro.");
            require(value.getRole()==EventRole.COMPETITOR || value.getRole()==EventRole.PARTICIPANT,"El rol seleccionado no admite resultados.");
            require(value.getPosition()==EventPosition.PARTICIPANT || value.getRole()==EventRole.COMPETITOR,"Los puestos de premiacion corresponden a competidores.");
        }
    }
    public int registerParticipant(EventParticipant value) {
        require(value!=null && value.getId()==null,"La nueva participacion no debe tener identificador.");id(value.getEventId());id(value.getStudentId());
        int id=transactions.execute(c -> {
            Event event=events.findById(value.getEventId(),c).orElseThrow(() -> new ValidationException("Evento no encontrado."));
            require(event.isActive(),"El evento esta inactivo.");
            LocalDate admission=context.requireStudent(value.getStudentId(),true,c);
            require(!event.getDate().isBefore(admission),"El evento es anterior al ingreso del estudiante al colegio.");
            validateParticipation(value,event);
            require(participants.findByPair(value.getEventId(),value.getStudentId(),c).isEmpty(),"El estudiante ya esta registrado en el evento.");
            return participants.create(value,c);
        });value.setId(id);return id;
    }
    public EventParticipant getParticipant(Integer participationId) {
        id(participationId);return transactions.execute(c -> participants.findById(participationId,c).orElseThrow(() -> new ValidationException("Participacion no encontrada.")));
    }
    public void updateParticipant(EventParticipant value) {
        require(value!=null,"La participacion es obligatoria.");id(value.getId());id(value.getEventId());id(value.getStudentId());
        transactions.execute(c -> {
            Event event=events.findById(value.getEventId(),c).orElseThrow(() -> new ValidationException("Evento no encontrado."));
            EventParticipant existing=participants.findByPair(value.getEventId(),value.getStudentId(),c).orElseThrow(() -> new ValidationException("Participacion no encontrada."));
            require(Objects.equals(existing.getId(),value.getId()),"No se puede cambiar el evento o estudiante de una participacion.");
            validateParticipation(value,event);participants.update(value,c);return null;
        });
    }
    public List<EventParticipant> findParticipantsByEvent(Integer eventId) { getById(eventId);return participants.findByEventId(eventId); }
    public List<EventParticipant> findParticipantsByStudent(Integer studentId) { id(studentId);return participants.findByStudentId(studentId); }
}
