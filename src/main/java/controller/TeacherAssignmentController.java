package controller;

import java.util.Objects;
import java.util.List;
import model.Subject;
import model.TeacherAssignment;
import model.ClassSchedule;
import service.TeacherAssignmentService;

public class TeacherAssignmentController {
    private final TeacherAssignmentService service;
    public TeacherAssignmentController() { this(new TeacherAssignmentService()); }
    public TeacherAssignmentController(TeacherAssignmentService service) { this.service = Objects.requireNonNull(service); }

    public Subject getSubjectById(Integer id) { return service.getSubjectById(id); }

    public List<Subject> findSubjects() { return service.findSubjects(); }

    public TeacherAssignment getById(Integer id) { return service.getById(id); }

    public int create(TeacherAssignment assignment) { return service.create(assignment); }

    public List<TeacherAssignment> findByTeacherAndYear(Integer teacherId, int year) { return service.findByTeacherAndYear(teacherId, year); }

    public int addSchedule(ClassSchedule schedule) { return service.addSchedule(schedule); }

    public List<ClassSchedule> findSchedulesByAssignment(Integer assignmentId) { return service.findSchedulesByAssignment(assignmentId); }

    public List<ClassSchedule> findSchedulesByTeacherAndYear(Integer teacherId, int year) { return service.findSchedulesByTeacherAndYear(teacherId, year); }

    public ClassSchedule getScheduleById(Integer id) { return service.getScheduleById(id); }

    public void deleteSchedule(Integer id) { service.deleteSchedule(id); }
}
