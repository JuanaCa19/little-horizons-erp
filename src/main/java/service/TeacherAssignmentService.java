package service;

import dao.*;
import dao.impl.*;
import java.sql.Connection;
import java.util.*;
import java.util.function.Supplier;
import model.*;
import model.enums.TeacherStatus;
import util.ConnectionDB;
import util.exception.ValidationException;
import static service.ServiceValidation.*;

public class TeacherAssignmentService {
    private final TeacherDAO teachers;
    private final SubjectDAO subjects;
    private final TeacherAssignmentDAO assignments;
    private final ClassScheduleDAO schedules;
    private final TransactionManager transactions;
    public TeacherAssignmentService() {
        this(new TeacherDAOImpl(), new SubjectDAOImpl(), new TeacherAssignmentDAOImpl(),
                new ClassScheduleDAOImpl(), ConnectionDB::getConnection);
    }
    public TeacherAssignmentService(TeacherDAO teachers, SubjectDAO subjects, TeacherAssignmentDAO assignments,
                                    ClassScheduleDAO schedules, Supplier<Connection> connections) {
        this.teachers = Objects.requireNonNull(teachers);
        this.subjects = Objects.requireNonNull(subjects);
        this.assignments = Objects.requireNonNull(assignments);
        this.schedules = Objects.requireNonNull(schedules);
        this.transactions = new TransactionManager(connections);
    }
    public Subject getSubjectById(Integer subjectId) {
        id(subjectId);
        return subjects.findById(subjectId).orElseThrow(() -> new ValidationException("Materia no encontrada."));
    }
    public List<Subject> findSubjects() { return subjects.findAll(); }
    public TeacherAssignment getById(Integer assignmentId) {
        id(assignmentId);
        return assignments.findById(assignmentId).orElseThrow(() -> new ValidationException("Asignacion no encontrada."));
    }
    private void requireTeacher(Integer teacherId) {
        id(teacherId);
        require(teachers.findById(teacherId).isPresent(), "Profesor no encontrado.");
    }
    private void requireActive(Integer teacherId, Connection connection) {
        Teacher teacher = teachers.findById(teacherId, connection)
                .orElseThrow(() -> new ValidationException("Profesor no encontrado."));
        require(teacher.getStatus() == TeacherStatus.ACTIVE, "Solo se pueden asignar clases a profesores activos.");
    }
    public int create(TeacherAssignment assignment) {
        require(assignment != null, "La asignacion es obligatoria.");
        require(assignment.getId() == null, "La nueva asignacion no debe tener identificador.");
        id(assignment.getTeacherId());
        year(assignment.getAcademicYear());
        String section = text(assignment.getSection(), "La seccion").toUpperCase(Locale.ROOT);
        require(section.length() <= 10, "La seccion admite hasta 10 caracteres.");
        Subject subject = getSubjectById(assignment.getSubjectId());
        require(subject.isActive(), "La materia esta inactiva.");
        TeacherAssignment pending = new TeacherAssignment();
        pending.setTeacherId(assignment.getTeacherId()); pending.setSubjectId(assignment.getSubjectId());
        pending.setAcademicYear(assignment.getAcademicYear()); pending.setSection(section);
        int id = transactions.execute(connection -> {
            teachers.lock(pending.getTeacherId(), connection);
            requireActive(pending.getTeacherId(), connection);
            require(!assignments.exists(pending.getTeacherId(), pending.getSubjectId(), pending.getAcademicYear(),
                    section, connection), "El profesor ya tiene esa materia, anio y seccion asignados.");
            return assignments.create(pending, connection);
        });
        assignment.setId(id); assignment.setSection(section);
        return id;
    }
    public List<TeacherAssignment> findByTeacherAndYear(Integer teacherId, int academicYear) {
        requireTeacher(teacherId); year(academicYear);
        return assignments.findByTeacherAndYear(teacherId, academicYear);
    }
    public int addSchedule(ClassSchedule schedule) {
        require(schedule != null, "El horario es obligatorio.");
        require(schedule.getId() == null, "El nuevo horario no debe tener identificador.");
        require(schedule.getWeekDay() != null, "El dia es obligatorio.");
        require(schedule.getStartTime() != null && schedule.getEndTime() != null
                && schedule.getEndTime().isAfter(schedule.getStartTime()), "La hora final debe ser posterior a la inicial.");
        require(schedule.getStartTime().getSecond() == 0 && schedule.getStartTime().getNano() == 0
                && schedule.getEndTime().getSecond() == 0 && schedule.getEndTime().getNano() == 0,
                "Use horas con precision de minutos.");
        TeacherAssignment assignment = getById(schedule.getAssignmentId());
        Subject subject = getSubjectById(assignment.getSubjectId());
        require(subject.isActive(), "La materia esta inactiva.");
        int id = transactions.execute(connection -> {
            // Serialize schedule changes for the same teacher or grade before checking conflicts.
            teachers.lock(assignment.getTeacherId(), connection);
            schedules.lockGrade(subject.getGradeId(), connection);
            requireActive(assignment.getTeacherId(), connection);
            require(!schedules.hasConflict(assignment, subject.getGradeId(), schedule, connection),
                    "El horario se cruza con otra clase del profesor o del grupo.");
            return schedules.create(schedule, connection);
        });
        schedule.setId(id);
        return id;
    }
    public List<ClassSchedule> findSchedulesByAssignment(Integer assignmentId) {
        getById(assignmentId);
        return schedules.findByAssignmentId(assignmentId);
    }
    public List<ClassSchedule> findSchedulesByTeacherAndYear(Integer teacherId, int academicYear) {
        requireTeacher(teacherId); year(academicYear);
        return schedules.findByTeacherAndYear(teacherId, academicYear);
    }
    public ClassSchedule getScheduleById(Integer scheduleId) {
        id(scheduleId);
        return schedules.findById(scheduleId).orElseThrow(() -> new ValidationException("Horario no encontrado."));
    }
    public void deleteSchedule(Integer scheduleId) {
        getScheduleById(scheduleId);
        schedules.delete(scheduleId);
    }
}
