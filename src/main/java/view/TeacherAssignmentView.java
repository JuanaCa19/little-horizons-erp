package view;

import controller.*;
import java.util.*;
import model.*;
import model.enums.WeekDay;
import static util.ConsoleUI.*;
import static util.InputReader.*;
import static view.ViewSupport.*;

public class TeacherAssignmentView {
    private final TeacherAssignmentController controller;
    private final TeacherController teachers;
    public TeacherAssignmentView() { this(new TeacherAssignmentController(), new TeacherController()); }
    public TeacherAssignmentView(TeacherAssignmentController controller, TeacherController teachers) {
        this.controller = Objects.requireNonNull(controller);
        this.teachers = Objects.requireNonNull(teachers);
    }
    public void start() {
        menu("Carga Academica y Horarios", "Volver", new String[]{"Consultar materias", "Asignar materia",
                "Consultar carga por profesor y anio", "Agregar horario", "Horarios por asignacion",
                "Horario del profesor por anio", "Eliminar horario"}, action(this::showSubjects), action(this::create),
                action(this::showWorkload), action(this::addSchedule), action(this::showAssignmentSchedule),
                action(this::showTeacherSchedule), action(this::deleteSchedule));
    }
    private int selectTeacher() {
        Teacher teacher = teachers.getById(readId("ID del profesor"));
        field("Profesor", teacher.getFullName());
        return teacher.getId();
    }
    private int readYear() { return readInt("Anio academico: ", 1900, 9999); }
    private void showSubjects() {
        printTable(List.of("ID", "Codigo", "Materia", "Grado ID", "Horas semanales", "Activa"),
                controller.findSubjects().stream().map(subject -> row(subject.getId(), subject.getCode(), subject.getName(),
                        subject.getGradeId(), subject.getWeeklyHours(), subject.isActive())).toList());
    }
    private void create() {
        TeacherAssignment assignment = new TeacherAssignment();
        assignment.setTeacherId(selectTeacher());
        showSubjects();
        Subject subject = controller.getSubjectById(readId("ID de la materia"));
        field("Materia", subject.getName());
        assignment.setSubjectId(subject.getId());
        assignment.setAcademicYear(readYear());
        assignment.setSection(readRequiredText("Seccion o grupo (ej: A): "));
        showAssignment(assignment);
        if (readYesNo("Guardar asignacion")) printSuccess("Asignacion creada. ID: " + controller.create(assignment));
        else printInfo("No se guardaron cambios.");
    }
    private void showWorkload() {
        int teacherId = selectTeacher();
        int year = readYear();
        List<TeacherAssignment> values = controller.findByTeacherAndYear(teacherId, year);
        printTable(List.of("Asignacion ID", "Materia", "Grado ID", "Anio", "Seccion"), values.stream().map(value -> {
            Subject subject = controller.getSubjectById(value.getSubjectId());
            return row(value.getId(), subject.getName(), subject.getGradeId(), value.getAcademicYear(), value.getSection());
        }).toList());
    }
    private TeacherAssignment selectAssignment() {
        TeacherAssignment assignment = controller.getById(readId("ID de asignacion"));
        showAssignment(assignment);
        return assignment;
    }
    private void showAssignment(TeacherAssignment value) {
        printHeader("Asignacion de materia");
        field("ID", value.getId()); field("Profesor", teachers.getById(value.getTeacherId()).getFullName());
        field("Materia", controller.getSubjectById(value.getSubjectId()).getName());
        field("Anio", value.getAcademicYear()); field("Seccion", value.getSection());
    }
    private void addSchedule() {
        TeacherAssignment assignment = selectAssignment();
        ClassSchedule schedule = new ClassSchedule();
        schedule.setAssignmentId(assignment.getId());
        schedule.setWeekDay(choose("Dia de clase", WeekDay.values(), WeekDay::getDbValue, null));
        schedule.setStartTime(readTime("Hora de inicio (HH:mm): "));
        schedule.setEndTime(readTime("Hora de fin (HH:mm): "));
        showSchedules(List.of(schedule));
        if (readYesNo("Guardar horario")) printSuccess("Horario creado. ID: " + controller.addSchedule(schedule));
        else printInfo("No se guardaron cambios.");
    }
    private void showAssignmentSchedule() { showSchedules(controller.findSchedulesByAssignment(selectAssignment().getId())); }
    private void showTeacherSchedule() {
        int teacherId = selectTeacher();
        showSchedules(controller.findSchedulesByTeacherAndYear(teacherId, readYear()));
    }
    private void deleteSchedule() {
        ClassSchedule schedule = controller.getScheduleById(readId("ID del horario"));
        showAssignment(controller.getById(schedule.getAssignmentId()));
        showSchedules(List.of(schedule));
        if (readYesNo("Eliminar este horario")) {
            controller.deleteSchedule(schedule.getId());
            printSuccess("Horario eliminado.");
        } else printInfo("No se guardaron cambios.");
    }
    private void showSchedules(List<ClassSchedule> values) {
        printTable(List.of("Horario ID", "Asignacion ID", "Materia", "Seccion", "Dia", "Inicio", "Fin"),
                values.stream().map(value -> {
                    TeacherAssignment assignment = controller.getById(value.getAssignmentId());
                    Subject subject = controller.getSubjectById(assignment.getSubjectId());
                    return row(value.getId(), value.getAssignmentId(), subject.getName(), assignment.getSection(),
                            value.getWeekDay().getDbValue(), value.getStartTime(), value.getEndTime());
                }).toList());
    }
}
