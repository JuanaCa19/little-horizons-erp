package integration;

import controller.*;
import dao.*;
import dao.impl.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import model.*;
import model.enums.*;
import service.*;
import util.exception.ValidationException;

/** Uses session-local tables that shadow the application tables; never changes existing records. */
public class TeacherModuleIntegrationTest {
    private static int checks;
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
    }
    private static void rejected(Runnable work) {
        try { work.run(); } catch (ValidationException e) { checks++; return; }
        throw new AssertionError("Expected business validation failure");
    }
    private static Teacher teacher(String document, String email) {
        Teacher teacher = new Teacher();
        teacher.setDocumentType(DocumentType.CITIZENSHIP_CARD);
        teacher.setDocumentNumber(document); teacher.setFirstName("Ana"); teacher.setFirstLastName("Pérez");
        teacher.setEmail(email); teacher.setSpecialization("Matemáticas");
        teacher.setHiringDate(LocalDate.of(2025, 1, 1)); teacher.setStatus(TeacherStatus.ACTIVE);
        return teacher;
    }
    private static TeacherAssignment assignment(int teacherId, int subjectId, int year, String section) {
        TeacherAssignment value = new TeacherAssignment();
        value.setTeacherId(teacherId); value.setSubjectId(subjectId); value.setAcademicYear(year); value.setSection(section);
        return value;
    }
    private static ClassSchedule schedule(int assignmentId, WeekDay day, String start, String end) {
        ClassSchedule value = new ClassSchedule();
        value.setAssignmentId(assignmentId); value.setWeekDay(day);
        value.setStartTime(LocalTime.parse(start)); value.setEndTime(LocalTime.parse(end));
        return value;
    }
    public static void main(String[] args) throws Exception {
        Properties config = new Properties();
        try (var reader = Files.newBufferedReader(Path.of("config.txt"))) { config.load(reader); }
        try (Connection real = DriverManager.getConnection(config.getProperty("url"), config.getProperty("user"), config.getProperty("password"))) {
            for (String table : List.of("profesores", "grados", "materias", "asignaciones_profesor", "horarios_asignacion")) {
                String ddl;
                try (PreparedStatement ps = real.prepareStatement("SHOW CREATE TABLE " + table); ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    ddl = rs.getString(2);
                }
                // MySQL rejects CREATE TEMPORARY TABLE t LIKE t. Reuse the live DDL without foreign keys.
                ddl = ddl.lines().filter(line -> !line.contains("FOREIGN KEY"))
                        .collect(java.util.stream.Collectors.joining("\n"))
                        .replaceFirst("CREATE TABLE", "CREATE TEMPORARY TABLE").replace(",\n)", "\n)");
                try (PreparedStatement ps = real.prepareStatement(ddl)) { ps.execute(); }
            }
            try (PreparedStatement ps = real.prepareStatement("INSERT INTO grados (id_grado,nombre_grado,nivel_numerico) VALUES (1,'Primero',1),(2,'Segundo',2)")) { ps.executeUpdate(); }
            try (PreparedStatement ps = real.prepareStatement("INSERT INTO materias (id_materia,codigo_materia,nombre_materia,id_grado,horas_semanales,esta_activa) VALUES (1,'MAT-1','Matematicas',1,5,1),(2,'ESP-1','Castellano',1,4,1),(3,'MAT-2','Matematicas',2,5,1),(4,'OFF-1','Inactiva',1,2,0)")) { ps.executeUpdate(); }
            // Services own and close connections; keep the shared temporary-table session alive until the test ends.
            Supplier<Connection> connections = () -> (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                    (proxy, method, values) -> {
                        if (method.getName().equals("close")) return null;
                        try { return method.invoke(real, values); }
                        catch (InvocationTargetException e) { throw e.getCause(); }
                    });
            TeacherDAO teacherDAO = new TeacherDAOImpl(connections);
            SubjectDAO subjectDAO = new SubjectDAOImpl(connections);
            TeacherAssignmentDAO assignmentDAO = new TeacherAssignmentDAOImpl(connections);
            ClassScheduleDAO scheduleDAO = new ClassScheduleDAOImpl(connections);
            TeacherController teachers = new TeacherController(new TeacherService(teacherDAO));
            TeacherAssignmentController assignments = new TeacherAssignmentController(new TeacherAssignmentService(teacherDAO,subjectDAO,assignmentDAO,scheduleDAO,connections));
            Teacher first = teacher("123", "ana@example.com");
            int firstId = teachers.create(first);
            check(first.getId() == firstId, "Generated teacher id");
            Teacher loaded = teachers.getById(firstId);
            check(loaded.getFullName().equals("Ana Pérez") && loaded.getHiringDate().equals(first.getHiringDate())
                    && loaded.getStatus() == TeacherStatus.ACTIVE, "Teacher JDBC mapping");
            check(teachers.findByDocument("123",DocumentType.CITIZENSHIP_CARD).isPresent(), "Document lookup");
            check(teachers.searchByName("Pérez").size() == 1, "Search with accents");
            rejected(() -> teachers.create(teacher("123","another@example.com")));
            rejected(() -> teachers.create(teacher("999","ANA@example.com")));
            Teacher invalid = teacher("999","invalid");
            rejected(() -> teachers.create(invalid));
            invalid.setEmail("other@example.com"); invalid.setDocumentType(DocumentType.IDENTITY_CARD);
            rejected(() -> teachers.create(invalid));
            loaded.setSpecialization("Física"); teachers.update(loaded);
            check(teachers.getById(firstId).getSpecialization().equals("Física"), "Update without losing status");
            Teacher second = teacher("456","second@example.com"); int secondId = teachers.create(second);
            check(teachers.findAll().size() == 2, "Directory");
            check(assignments.findSubjects().size() == 4, "Subject catalog mapping");
            TeacherAssignment firstAssignment = assignment(firstId,1,2026," a ");
            int firstAssignmentId = assignments.create(firstAssignment);
            check(firstAssignment.getSection().equals("A"), "Normalize section");
            check(assignments.getById(firstAssignmentId).getTeacherId() == firstId, "Assignment mapping");
            TeacherAssignment duplicate = assignment(firstId,1,2026,"A");
            rejected(() -> assignments.create(duplicate));
            check(duplicate.getId() == null, "Failed assignment keeps null id");
            rejected(() -> assignments.create(assignment(firstId,4,2026,"A")));
            teachers.updateStatus(secondId, TeacherStatus.INACTIVE);
            rejected(() -> assignments.create(assignment(secondId,2,2026,"A")));
            teachers.updateStatus(secondId, TeacherStatus.ON_LEAVE);
            rejected(() -> assignments.create(assignment(secondId,2,2026,"A")));
            teachers.updateStatus(secondId, TeacherStatus.ACTIVE);
            int secondAssignmentId = assignments.create(assignment(secondId,2,2026,"A"));
            int otherGradeId = assignments.create(assignment(firstId,3,2026,"A"));
            int otherSectionId = assignments.create(assignment(secondId,1,2026,"B"));
            int otherYearId = assignments.create(assignment(firstId,1,2027,"A"));
            check(assignments.findByTeacherAndYear(firstId,2026).size() == 2, "Year-filtered workload");
            ClassSchedule firstClass = schedule(firstAssignmentId,WeekDay.WEDNESDAY,"08:00","09:00");
            int firstScheduleId = assignments.addSchedule(firstClass);
            check(assignments.getScheduleById(firstScheduleId).getWeekDay() == WeekDay.WEDNESDAY, "Accented day mapping");
            // Another subject in the same grade and section cannot overlap, even with another teacher.
            ClassSchedule groupConflict = schedule(secondAssignmentId,WeekDay.WEDNESDAY,"08:30","09:30");
            rejected(() -> assignments.addSchedule(groupConflict));
            check(groupConflict.getId() == null && assignments.findSchedulesByAssignment(secondAssignmentId).isEmpty(), "Conflict rolls back");
            // The same teacher cannot overlap across grades.
            rejected(() -> assignments.addSchedule(schedule(otherGradeId,WeekDay.WEDNESDAY,"08:30","09:30")));
            rejected(() -> assignments.addSchedule(schedule(firstAssignmentId,WeekDay.WEDNESDAY,"08:00","09:00")));
            rejected(() -> assignments.addSchedule(schedule(firstAssignmentId,WeekDay.MONDAY,"09:00","08:00")));
            rejected(() -> assignments.addSchedule(schedule(firstAssignmentId,WeekDay.MONDAY,"08:00","08:00")));
            assignments.addSchedule(schedule(secondAssignmentId,WeekDay.WEDNESDAY,"09:00","10:00"));
            assignments.addSchedule(schedule(otherSectionId,WeekDay.WEDNESDAY,"08:00","09:00"));
            assignments.addSchedule(schedule(otherYearId,WeekDay.WEDNESDAY,"08:00","09:00"));
            assignments.addSchedule(schedule(firstAssignmentId,WeekDay.SATURDAY,"08:00","09:00"));
            check(assignments.findSchedulesByTeacherAndYear(firstId,2026).size() == 2, "Teacher timetable and boundary rules");
            teachers.updateStatus(firstId,TeacherStatus.ON_LEAVE);
            rejected(() -> assignments.addSchedule(schedule(firstAssignmentId,WeekDay.MONDAY,"10:00","11:00")));
            check(assignments.findSchedulesByAssignment(firstAssignmentId).size() == 2, "Status changes preserve history");
            assignments.deleteSchedule(firstScheduleId);
            check(assignments.findSchedulesByAssignment(firstAssignmentId).size() == 1, "Schedule removal");
            rejected(() -> assignments.getScheduleById(firstScheduleId));
            real.rollback();
        }
        System.out.println("Passed " + checks + " teacher module JDBC checks using temporary tables.");
    }
}
