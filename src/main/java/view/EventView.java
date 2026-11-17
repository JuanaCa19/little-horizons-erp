package view;

import controller.*;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.*;
import static util.ConsoleUI.*;
import static util.InputReader.*;
import static view.ViewSupport.*;

public class EventView {
    private final EventController controller;
    private final StudentController students;
    private final TeacherController teachers;
    public EventView() { this(new EventController(),new StudentController(),new TeacherController()); }
    public EventView(EventController controller,StudentController students,TeacherController teachers) {
        this.controller=Objects.requireNonNull(controller);this.students=Objects.requireNonNull(students);this.teachers=Objects.requireNonNull(teachers);
    }
    public void start() {
        menu("Eventos del Colegio","Volver",new String[]{"Crear evento","Calendario por fechas","Consultar detalle","Actualizar evento",
                "Registrar participante","Participantes y resultados del evento","Actualizar participacion o resultado","Historial de eventos del estudiante"},
                action(this::create),action(this::calendar),action(() -> showDetails(selectEvent())),action(this::update),action(this::register),
                action(() -> showParticipants(controller.findParticipantsByEvent(selectEvent().getId()))),action(this::updateParticipant),
                action(() -> showParticipants(controller.findParticipantsByStudent(selectStudent()))));
    }
    private Event selectEvent() {
        Event event=controller.getById(readId("ID del evento"));field("Evento",event.getName());return event;
    }
    private int selectStudent() {
        Student student=students.getById(readId("ID del estudiante"));field("Estudiante",student.getFullName());return student.getId();
    }
    private Integer organizer(Integer current) {
        if(current!=null && !readYesNo("Cambiar organizador ["+current+"]")) return current;
        if(!readYesNo("Asignar profesor organizador")) return null;
        Teacher teacher=teachers.getById(readId("ID del profesor"));field("Organizador",teacher.getFullName());return teacher.getId();
    }
    private Event readForm(Event current) {
        Event value=new Event();value.setId(current==null ? null : current.getId());
        if(current!=null) printInfo("Enter conserva los textos actuales. - borra los campos opcionales.");
        value.setName(required("Nombre del evento",current==null ? null : current.getName()));
        value.setDescription(optional("Descripcion",current==null ? null : current.getDescription()));
        value.setType(choose("Tipo de evento",EventType.values(),EventType::getDbValue,current==null ? null : current.getType()));
        value.setDate(date("Fecha del evento",current==null ? null : current.getDate()));
        value.setLocation(optional("Lugar",current==null ? null : current.getLocation()));
        value.setCompetition(readYesNo("Tiene competencia"));value.setOrganizerId(organizer(current==null ? null : current.getOrganizerId()));
        value.setActive(readYesNo("Evento activo"));return value;
    }
    private void create() {
        Event value=readForm(null);showDetails(value);
        if(readYesNo("Guardar evento")) printSuccess("Evento creado. ID: "+controller.create(value));else printInfo("No se guardaron cambios.");
    }
    private void update() {
        Event value=readForm(selectEvent());showDetails(value);
        if(readYesNo("Guardar cambios")) { controller.update(value);printSuccess("Evento actualizado."); } else printInfo("No se guardaron cambios.");
    }
    private void calendar() {
        LocalDate from=readDate("Desde (dd/MM/yyyy): ");LocalDate to=readDate("Hasta (dd/MM/yyyy): ");
        printTable(List.of("ID","Evento","Fecha","Tipo","Lugar","Competencia","Activo"),controller.findByDates(from,to).stream().map(value -> row(value.getId(),value.getName(),value.getDate(),
                value.getType().getDbValue(),value.getLocation(),value.isCompetition(),value.isActive())).toList());
    }
    private void showDetails(Event value) {
        printHeader("Detalle del evento");field("ID",value.getId());field("Nombre",value.getName());field("Descripcion",value.getDescription());
        field("Tipo",value.getType().getDbValue());field("Fecha",value.getDate());field("Lugar",value.getLocation());field("Competencia",value.isCompetition());
        field("Organizador",value.getOrganizerId()==null ? null : teachers.getById(value.getOrganizerId()).getFullName());field("Activo",value.isActive());
    }
    private EventParticipant readParticipation(Event event,Integer studentId,EventParticipant current) {
        EventParticipant value=new EventParticipant();value.setId(current==null ? null : current.getId());value.setEventId(event.getId());value.setStudentId(studentId);
        EventRole[] roles=Arrays.stream(EventRole.values()).filter(role -> event.isCompetition() || role!=EventRole.COMPETITOR).toArray(EventRole[]::new);
        value.setRole(choose("Rol de participacion",roles,EventRole::getDbValue,current==null ? null : current.getRole()));
        boolean resultAllowed=event.isCompetition() && !event.getDate().isAfter(LocalDate.now()) && (value.getRole()==EventRole.COMPETITOR || value.getRole()==EventRole.PARTICIPANT);
        if(resultAllowed && readYesNo("Registrar posicion o resultado")) {
            EventPosition[] positions=value.getRole()==EventRole.COMPETITOR ? EventPosition.values() : new EventPosition[]{EventPosition.PARTICIPANT};
            value.setPosition(choose("Posicion obtenida",positions,EventPosition::getDbValue,null));
        }
        value.setNotes(optional("Observaciones",current==null ? null : current.getNotes()));return value;
    }
    private void register() {
        Event event=selectEvent();showDetails(event);EventParticipant value=readParticipation(event,selectStudent(),null);showParticipants(List.of(value));
        if(readYesNo("Guardar participacion")) printSuccess("Participante registrado. ID: "+controller.registerParticipant(value));else printInfo("No se guardaron cambios.");
    }
    private void updateParticipant() {
        EventParticipant current=controller.getParticipant(readId("ID de participacion"));showParticipants(List.of(current));
        EventParticipant value=readParticipation(controller.getById(current.getEventId()),current.getStudentId(),current);showParticipants(List.of(value));
        if(readYesNo("Guardar cambios de participacion")) { controller.updateParticipant(value);printSuccess("Participacion actualizada."); } else printInfo("No se guardaron cambios.");
    }
    private void showParticipants(List<EventParticipant> values) {
        printTable(List.of("Participacion ID","Evento","Estudiante","Rol","Posicion","Observaciones"),values.stream().map(value -> row(value.getId(),controller.getById(value.getEventId()).getName(),
                students.getById(value.getStudentId()).getFullName(),value.getRole().getDbValue(),value.getPosition()==null ? null : value.getPosition().getDbValue(),value.getNotes())).toList());
    }
}
