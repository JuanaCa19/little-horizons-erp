package view;

import controller.*;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.MembershipStatus;
import static util.ConsoleUI.*;
import static util.InputReader.*;
import static view.ViewSupport.*;

public class ClubView {
    private final ClubController controller;
    private final StudentController students;
    private final TeacherController teachers;
    public ClubView() { this(new ClubController(),new StudentController(),new TeacherController()); }
    public ClubView(ClubController controller,StudentController students,TeacherController teachers) {
        this.controller=Objects.requireNonNull(controller);this.students=Objects.requireNonNull(students);this.teachers=Objects.requireNonNull(teachers);
    }
    public void start() {
        menu("Clubes Extracurriculares","Volver",new String[]{"Crear club","Listar clubes y cupos","Consultar detalle","Actualizar club",
                "Inscribir estudiante","Consultar miembros del club","Retirar estudiante","Historial de clubes del estudiante"},
                action(this::create),action(this::list),action(() -> showDetails(selectClub())),action(this::update),action(this::join),
                action(() -> showMemberships(controller.findMembershipsByClub(selectClub().getId()))),action(this::withdraw),
                action(() -> showMemberships(controller.findMembershipsByStudent(selectStudent()))));
    }
    private Club selectClub() {
        Club club=controller.getById(readId("ID del club"));field("Club",club.getName());return club;
    }
    private int selectStudent() {
        Student student=students.getById(readId("ID del estudiante"));field("Estudiante",student.getFullName());return student.getId();
    }
    private Integer supervisor(Integer current) {
        if(current!=null && !readYesNo("Cambiar profesor encargado ["+current+"]")) return current;
        if(!readYesNo("Asignar profesor encargado")) return null;
        Teacher teacher=teachers.getById(readId("ID del profesor"));field("Profesor",teacher.getFullName());return teacher.getId();
    }
    private Club readForm(Club current) {
        Club value=new Club();value.setId(current==null ? null : current.getId());
        if(current!=null) printInfo("Enter conserva los textos actuales. - borra los campos opcionales.");
        value.setName(required("Nombre del club",current==null ? null : current.getName()));
        value.setDescription(optional("Descripcion",current==null ? null : current.getDescription()));
        value.setSupervisorId(supervisor(current==null ? null : current.getSupervisorId()));
        value.setCapacity(current!=null && !readYesNo("Cambiar cupo ["+current.getCapacity()+"]") ? current.getCapacity() : readInt("Cupo maximo: ",1,Integer.MAX_VALUE));
        value.setMeetingSchedule(optional("Horario de reunion",current==null ? null : current.getMeetingSchedule()));
        value.setActive(readYesNo("Club activo"));return value;
    }
    private void create() {
        Club value=readForm(null);showDetails(value);
        if(readYesNo("Guardar club")) printSuccess("Club creado. ID: "+controller.create(value));else printInfo("No se guardaron cambios.");
    }
    private void update() {
        Club value=readForm(selectClub());showDetails(value);
        if(readYesNo("Guardar cambios")) { controller.update(value);printSuccess("Club actualizado."); } else printInfo("No se guardaron cambios.");
    }
    private void list() {
        printTable(List.of("ID","Club","Profesor ID","Cupo","Disponibles","Horario","Activo"),controller.findAll().stream().map(value -> row(value.getId(),value.getName(),value.getSupervisorId(),
                value.getCapacity(),controller.availableSeats(value.getId()),value.getMeetingSchedule(),value.isActive())).toList());
    }
    private void showDetails(Club value) {
        printHeader("Detalle del club");field("ID",value.getId());field("Nombre",value.getName());field("Descripcion",value.getDescription());
        field("Profesor encargado",value.getSupervisorId()==null ? null : teachers.getById(value.getSupervisorId()).getFullName());
        field("Cupo maximo",value.getCapacity());if(value.getId()!=null) field("Cupos disponibles",controller.availableSeats(value.getId()));
        field("Horario",value.getMeetingSchedule());field("Activo",value.isActive());
    }
    private void join() {
        Club club=selectClub();showDetails(club);
        ClubMembership value=new ClubMembership();value.setClubId(club.getId());value.setStudentId(selectStudent());value.setStatus(MembershipStatus.ACTIVE);
        value.setEntryDate(readDate("Fecha de ingreso al club (dd/MM/yyyy): "));showMemberships(List.of(value));
        if(readYesNo("Confirmar inscripcion")) printSuccess("Membresia creada. ID: "+controller.join(value));else printInfo("No se guardaron cambios.");
    }
    private void withdraw() {
        ClubMembership value=controller.getMembership(readId("ID de membresia"));showMemberships(List.of(value));
        LocalDate date=readDate("Fecha de retiro (dd/MM/yyyy): ");String reason=readRequiredText("Motivo de retiro: ");
        if(readYesNo("Confirmar retiro del club")) { controller.withdraw(value.getId(),date,reason);printSuccess("Retiro registrado. El historial se conserva."); }
        else printInfo("No se guardaron cambios.");
    }
    private void showMemberships(List<ClubMembership> values) {
        printTable(List.of("Membresia ID","Estudiante","Club","Ingreso","Estado","Retiro","Motivo"),values.stream().map(value -> row(value.getId(),
                students.getById(value.getStudentId()).getFullName(),controller.getById(value.getClubId()).getName(),value.getEntryDate(),value.getStatus().getDbValue(),
                value.getWithdrawalDate(),value.getWithdrawalReason())).toList());
    }
}
