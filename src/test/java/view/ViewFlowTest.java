package view;

import controller.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.*;
import util.exception.*;

/** Run each scenario in a fresh JVM because InputReader owns a shared Scanner. */
public class ViewFlowTest {
    public static void main(String[] args) {
        String scenario = args[0];
        Student original = new Student(1, DocumentType.IDENTITY_CARD, "123", "Camila", null, "Ardila", null,
                "Carrera 27", null, null, "O+", null, null, Gender.FEMALE, LocalDate.of(2014, 5, 12),
                LocalDate.of(2026, 1, 15), StudentStatus.ACTIVE);
        Guardian guardian = new Guardian(2, DocumentType.CITIZENSHIP_CARD, "987", "Ana", null, "Ardila", null,
                "3101234567", null, null, "Carrera 27", null);
        int[] writes = {0};
        StudentController students = new StudentController() {
            @Override public Student getById(Integer id) { return original; }
            @Override public List<Student> findAll() {
                if (scenario.equals("validation-error")) throw new ValidationException("Validation failure");
                if (scenario.equals("database-error")) throw new DataAccessException("Database failure");
                return List.of(original);
            }
            @Override public int register(Student student, Guardian selected, StudentGuardian relation, Enrollment enrollment) {
                ensure(student.getFirstName().equals("Camila"), "Student form was not read");
                ensure(selected.getFirstName().equals("Ana"), "Guardian was not selected or read");
                ensure(relation.isPrimary() && relation.isFinancialResponsible(), "Relation flags lost");
                ensure(enrollment.getGradeId() == 1 && enrollment.getAcademicYear() == 2026, "Enrollment form incorrect");
                ensure(scenario.equals("register-existing") ? selected.getId() == 2 : selected.getId() == null,
                        "Wrong guardian registration mode");
                writes[0]++;
                student.setId(10);
                enrollment.setId(30);
                return 10;
            }
            @Override public void update(Student student) { writes[0]++; }
        };
        GuardianController guardians = new GuardianController() {
            @Override public Guardian getById(Integer id) { return guardian; }
        };
        GradeController grades = new GradeController() {
            @Override public List<Grade> findAll() {
                Grade grade = new Grade();
                grade.setId(1);
                grade.setName("Sexto");
                grade.setNumericLevel(6);
                return List.of(grade);
            }
        };
        StudentView view = new StudentView(students, new GuardianView(guardians),
                new EnrollmentView(new EnrollmentController(), students, new GradeView(grades)),
                new StudentGuardianView(new StudentGuardianController(), students, guardians));
        String input = switch (scenario) {
            case "navigation" -> "texto\n9\n1\n8\n7\n12\n8\n";
            case "cancel-registration" -> "1\ncancelar\n12\n";
            case "cancel-update" -> "6\n1\nno\n\nChanged\n\n\n\ncancelar\n12\n";
            case "validation-error", "database-error" -> "2\n\n12\n";
            case "list" -> "2\n\n12\n";
            case "register-existing", "register-new", "decline-registration" -> registrationInput(scenario);
            default -> throw new IllegalArgumentException(scenario);
        };
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream console = System.out;
        PrintStream errors = System.err;
        try (PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            System.setErr(capture);
            if (scenario.equals("navigation")) new MainMenuView(view).start();
            else view.start();
        } finally {
            System.setOut(console);
            System.setErr(errors);
        }
        String output = buffer.toString(StandardCharsets.UTF_8);
        ensure(original.getFirstName().equals("Camila"), "Cancelled edit mutated original student");
        boolean saved = scenario.equals("register-existing") || scenario.equals("register-new");
        ensure(writes[0] == (saved ? 1 : 0), "Unexpected number of writes");
        if (scenario.startsWith("cancel-")) ensure(output.contains("Operacion cancelada"), "Missing cancellation message");
        if (saved) ensure(output.contains("Estudiante registrado. ID: 10"), "Missing success message");
        if (scenario.equals("validation-error")) ensure(output.contains("Validation failure"), "Validation error hidden");
        if (scenario.equals("database-error")) ensure(output.contains("Database failure"), "Database error hidden");
        if (scenario.equals("list")) ensure(output.contains("Camila Ardila"), "Student list not rendered");
        if (scenario.equals("navigation")) ensure(output.contains("Hasta pronto"), "Could not exit main menu");
        System.out.println("Passed view scenario: " + scenario);
    }

    private static String registrationInput(String scenario) {
        List<String> lines = new ArrayList<>(List.of("1", "2", "123", "Camila", "", "Ardila", "", "12/05/2014",
                "2", "Carrera 27", "", "", "O+", "", "", "15/01/2026"));
        if (scenario.equals("register-new")) {
            lines.addAll(List.of("no", "3", "987", "Ana", "", "Ardila", "", "3101234567", "", "", "Carrera 27", ""));
        } else lines.addAll(List.of("si", "2"));
        lines.addAll(List.of("Madre", "si", "si", "1", "2026", "16/01/2026", "",
                scenario.equals("decline-registration") ? "no" : "si", "", "12"));
        return String.join("\n", lines) + "\n";
    }

    private static void ensure(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
