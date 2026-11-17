package service;

import dao.*;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import model.*;
import model.enums.*;
import util.exception.*;

/** Standalone service tests; no database or external testing library is needed. */
public class StudentModuleServiceTest {
    private static int checks;

    public static void main(String[] args) {
        testRegistration(false, false);
        testRegistration(true, false);
        testRegistration(false, true);
        testValidation();
        testGuardianProtection();
        testEnrollmentAndStudentStatus();
        testTransactionFailures();
        System.out.println("Passed " + checks + " service checks.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }

    private static void fails(Class<? extends Throwable> type, Runnable action) {
        try {
            action.run();
        } catch (Throwable error) {
            check(type.isInstance(error), "Unexpected failure: " + error);
            return;
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    private static <T> T stub(Class<T> type, BiFunction<String, Object[], Object> handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> handler.apply(method.getName(), args));
    }

    private static Student student() {
        return new Student(null, DocumentType.IDENTITY_CARD, "123456789", "Camila", null,
                "Ardila", null, "Carrera 27", null, "camila@example.com", null, null, null,
                Gender.FEMALE, LocalDate.of(2014, 5, 12), LocalDate.of(2026, 1, 15), StudentStatus.ACTIVE);
    }

    private static Guardian guardian() {
        return new Guardian(null, DocumentType.CITIZENSHIP_CARD, "987654321", "Ana", null,
                "Ardila", null, "3101234567", null, "ana@example.com", "Carrera 27", null);
    }

    private static Enrollment enrollment() {
        return new Enrollment(null, null, 1, 2026, null, LocalDate.of(2026, 1, 16), null, null);
    }

    private static void testRegistration(boolean existingGuardian, boolean failEnrollment) {
        List<String> events = new ArrayList<>();
        Connection connection = stub(Connection.class, (name, args) -> {
            events.add(name);
            return null;
        });
        Student student = student();
        Guardian guardian = guardian();
        if (existingGuardian) guardian.setId(20);
        StudentGuardian relation = new StudentGuardian(null, null, "Madre", false, true, true);
        Enrollment enrollment = enrollment();
        StudentDAO students = stub(StudentDAO.class, (name, args) -> {
            if (name.equals("existsByDocument")) return false;
            check(name.equals("create") && args.length == 2 && args[1] == connection,
                    "Student must use the transaction connection");
            return 10;
        });
        GuardianDAO guardians = stub(GuardianDAO.class, (name, args) -> {
            if (name.equals("existsByDocument")) return false;
            if (name.equals("findById")) return Optional.of(guardian);
            check(!existingGuardian && args[1] == connection, "Existing guardian must not be inserted");
            return 20;
        });
        StudentGuardianDAO relations = stub(StudentGuardianDAO.class, (name, args) -> {
            StudentGuardian value = (StudentGuardian) args[0];
            check(args[1] == connection && value.getStudentId() == 10 && value.getGuardianId() == 20
                    && value.isPrimary(), "Relation must use generated ids and the shared connection");
            return null;
        });
        GradeDAO grades = stub(GradeDAO.class, (name, args) -> Optional.of(new Grade()));
        EnrollmentDAO enrollments = stub(EnrollmentDAO.class, (name, args) -> {
            check(args[1] == connection && ((Enrollment) args[0]).getStudentId() == 10,
                    "Enrollment must use the shared connection");
            if (failEnrollment) throw new DataAccessException("Simulated insert failure");
            return 30;
        });
        StudentService service = new StudentService(students, guardians, relations, enrollments, grades, () -> connection);
        if (failEnrollment) {
            fails(DataAccessException.class, () -> service.register(student, guardian, relation, enrollment));
            check(events.contains("rollback") && !events.contains("commit"), "Failure must roll back");
            check(student.getId() == null && guardian.getId() == null && enrollment.getId() == null
                    && relation.getStudentId() == null, "Rollback must not publish generated ids");
        } else {
            check(service.register(student, guardian, relation, enrollment) == 10, "Return student id");
            check(events.contains("commit") && !events.contains("rollback"), "Success must commit");
            check(student.getId() == 10 && guardian.getId() == 20 && enrollment.getId() == 30
                    && relation.isPrimary(), "Assign generated ids after commit");
        }
        check(events.get(events.size() - 1).equals("close"), "Always close the connection");
    }

    private static void testValidation() {
        ServiceValidation.student(student());
        ServiceValidation.guardian(guardian());
        check(true, "Valid email addresses must be accepted");
        Student invalid = student();
        invalid.setEmail("invalid-email");
        fails(ValidationException.class, () -> ServiceValidation.student(invalid));
        invalid.setEmail("name with space@example.com");
        fails(ValidationException.class, () -> ServiceValidation.student(invalid));
        invalid.setEmail(null);
        invalid.setBirthDate(LocalDate.now().plusDays(1));
        fails(ValidationException.class, () -> ServiceValidation.student(invalid));
        GuardianDAO duplicate = stub(GuardianDAO.class, (name, args) -> true);
        fails(ValidationException.class, () -> new GuardianService(duplicate).create(guardian()));
    }

    private static void testGuardianProtection() {
        StudentGuardian relation = new StudentGuardian(2, 1, "Madre", true, true, true);
        StudentGuardianDAO relations = stub(StudentGuardianDAO.class, (name, args) -> switch (name) {
            case "findByIds" -> Optional.of(relation);
            case "findByStudentId" -> List.of(relation);
            default -> throw new AssertionError("Unexpected write: " + name);
        });
        StudentGuardianService service = new StudentGuardianService(relations,
                stub(StudentDAO.class, (name, args) -> Optional.of(student())),
                stub(GuardianDAO.class, (name, args) -> Optional.of(guardian())));
        fails(ValidationException.class, () -> service.unlink(1, 2));
        fails(ValidationException.class, () -> service.link(relation));
    }

    private static void testEnrollmentAndStudentStatus() {
        Enrollment existing = enrollment();
        existing.setId(3);
        existing.setStudentId(1);
        existing.setStatus(EnrollmentStatus.ACTIVE);
        Student student = student();
        student.setId(1);
        StudentDAO students = stub(StudentDAO.class, (name, args) -> Optional.of(student));
        EnrollmentDAO enrollments = stub(EnrollmentDAO.class, (name, args) -> switch (name) {
            case "findById", "findByStudentAndYear" -> Optional.of(existing);
            case "findByStudentId" -> List.of(existing);
            default -> throw new AssertionError("Unexpected write: " + name);
        });
        GradeDAO grades = stub(GradeDAO.class, (name, args) -> Optional.of(new Grade()));
        GuardianDAO guardians = stub(GuardianDAO.class, (name, args) -> null);
        StudentGuardianDAO relations = stub(StudentGuardianDAO.class, (name, args) ->
                List.of(new StudentGuardian(2, 1, "Madre", true, true, true)));
        EnrollmentService service = new EnrollmentService(enrollments, students, grades, relations);
        Enrollment duplicate = enrollment();
        duplicate.setStudentId(1);
        fails(ValidationException.class, () -> service.create(duplicate));
        fails(ValidationException.class, () -> service.updateStatus(3, EnrollmentStatus.WITHDRAWN,
                LocalDate.of(2025, 1, 1)));
        StudentService studentService = new StudentService(students, guardians, relations, enrollments,
                grades, () -> null);
        fails(ValidationException.class, () -> studentService.updateStatus(1, StudentStatus.GRADUATED));
    }

    private static void testTransactionFailures() {
        fails(DataAccessException.class, () -> new TransactionManager(() -> null).execute(connection -> 1));
        List<String> events = new ArrayList<>();
        Connection connection = stub(Connection.class, (name, args) -> {
            events.add(name);
            if (name.equals("commit")) throw new DataAccessException("Commit failed");
            return null;
        });
        fails(DataAccessException.class, () -> new TransactionManager(() -> connection).execute(value -> 1));
        check(events.contains("rollback") && events.contains("close"), "Commit failure must roll back and close");
    }
}
