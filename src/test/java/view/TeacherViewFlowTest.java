package view;

import controller.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import model.*;
import model.enums.*;
import util.exception.DataAccessException;

/** Each scenario runs in a fresh JVM to isolate InputReader's shared Scanner. */
public class TeacherViewFlowTest {
    private static void ensure(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        String scenario = args[0];
        Teacher original = new Teacher();
        original.setId(1); original.setDocumentType(DocumentType.CITIZENSHIP_CARD); original.setDocumentNumber("123");
        original.setFirstName("Ana"); original.setFirstLastName("Perez"); original.setEmail("ana@example.com");
        original.setHiringDate(LocalDate.of(2025,1,1)); original.setStatus(TeacherStatus.ACTIVE);
        Subject subject = new Subject();
        subject.setId(1); subject.setName("Matematicas"); subject.setCode("MAT-1"); subject.setGradeId(1); subject.setActive(true);
        TeacherAssignment assignment = new TeacherAssignment();
        assignment.setId(2); assignment.setTeacherId(1); assignment.setSubjectId(1); assignment.setAcademicYear(2026); assignment.setSection("A");
        int[] writes = {0};
        TeacherController teachers = new TeacherController() {
            @Override public Teacher getById(Integer id) { return original; }
            @Override public List<Teacher> findAll() {
                if (scenario.equals("database-error")) throw new DataAccessException("Database failure");
                return List.of(original);
            }
            @Override public int create(Teacher value) {
                ensure(value.getId() == null && value.getFirstName().equals("Ana") && value.getStatus() == TeacherStatus.ACTIVE,
                        "Invalid professor form");
                writes[0]++; return 10;
            }
            @Override public void update(Teacher value) {
                ensure(value != original && value.getEmail().equals(original.getEmail()) && value.getStatus() == original.getStatus(),
                        "Edit did not preserve fields or used original instance");
                writes[0]++;
            }
            @Override public void updateStatus(Integer id, TeacherStatus status) {
                ensure(id == 1 && status == TeacherStatus.ON_LEAVE, "Wrong status selection"); writes[0]++;
            }
        };
        TeacherAssignmentController classes = new TeacherAssignmentController() {
            @Override public TeacherAssignment getById(Integer id) { return assignment; }
            @Override public Subject getSubjectById(Integer id) { return subject; }
            @Override public List<Subject> findSubjects() { return List.of(subject); }
            @Override public int addSchedule(ClassSchedule value) {
                ensure(value.getAssignmentId() == 2 && value.getWeekDay() == WeekDay.MONDAY
                        && value.getStartTime().equals(LocalTime.of(8,0)) && value.getEndTime().equals(LocalTime.of(9,0)),
                        "Invalid schedule form or time retry");
                writes[0]++; return 20;
            }
        };
        TeacherAssignmentView workload = new TeacherAssignmentView(classes, teachers);
        TeacherView view = new TeacherView(teachers, workload);
        String form = "1\n1\n123\nAna\n\nPerez\n\nana@example.com\n\nMatematicas\n01/01/2025\n";
        String input = switch (scenario) {
            case "navigation" -> "3\n8\n8\n9\n8\n";
            case "register" -> form + "si\n\n9\n";
            case "decline" -> form + "no\n\n9\n";
            case "cancel" -> "1\ncancelar\n9\n";
            case "cancel-update" -> "6\n1\nno\n\nChanged\ncancelar\n9\n";
            case "update-preserve" -> "6\n1\nno\n\n\n\n\n\n\n\n\nno\nsi\n\n9\n";
            case "list", "database-error" -> "2\n\n9\n";
            case "status" -> "7\n1\n3\nsi\n\n9\n";
            case "schedule" -> "4\n2\n1\n24:00\nwrong\n08:00\n09:00\nsi\n\n8\n";
            case "cancel-time" -> "4\n2\n1\ncancelar\n8\n";
            default -> throw new IllegalArgumentException(scenario);
        };
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream console = System.out, errors = System.err;
        try (PrintStream capture = new PrintStream(output,true,StandardCharsets.UTF_8)) {
            System.setOut(capture); System.setErr(capture);
            if (scenario.equals("navigation")) new MainMenuView(new StudentView(),view).start();
            else if (scenario.equals("schedule") || scenario.equals("cancel-time")) workload.start();
            else view.start();
        } finally { System.setOut(console); System.setErr(errors); }
        String text = output.toString(StandardCharsets.UTF_8);
        boolean saved = Set.of("register","update-preserve","status","schedule").contains(scenario);
        ensure(writes[0] == (saved ? 1 : 0), "Unexpected database write");
        ensure(original.getFirstName().equals("Ana"), "View mutated original teacher");
        if (scenario.equals("list")) ensure(text.contains("Ana Perez"), "Directory missing");
        if (scenario.equals("database-error")) ensure(text.contains("Database failure"), "Error not displayed");
        if (scenario.equals("schedule")) ensure(text.contains("Hora invalida") && text.contains("Horario creado"), "No time validation or confirmation");
        if (scenario.startsWith("cancel")) ensure(text.contains("Operacion cancelada"), "Cancellation missing");
        if (scenario.equals("navigation")) ensure(text.contains("Hasta pronto"), "Cannot exit main menu");
        System.out.println("Passed teacher view scenario: " + scenario);
    }
}
