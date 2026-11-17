package controller;

import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import model.*;
import model.enums.*;
import service.AcademicService;

public class AcademicController {
    private final AcademicService service;
    public AcademicController() { this(new AcademicService()); }
    public AcademicController(AcademicService service) { this.service=Objects.requireNonNull(service); }

    public int enroll(SubjectEnrollment value) { return service.enroll(value); }

    public SubjectEnrollment getEnrollment(Integer id) { return service.getEnrollment(id); }

    public void updateEnrollmentStatus(Integer id, EnrollmentStatus status) { service.updateEnrollmentStatus(id,status); }

    public List<SubjectEnrollment> findEnrollments(Integer studentId, int year) { return service.findEnrollments(studentId,year); }

    public List<SubjectEnrollment> findClassEnrollments(Integer subjectId, int year, AcademicPeriod period) { return service.findClassEnrollments(subjectId,year,period); }

    public int saveRecord(AcademicRecord value) { return service.saveRecord(value); }

    public AcademicRecord getRecord(Integer id) { return service.getRecord(id); }

    public List<AcademicRecord> findHistory(Integer studentId) { return service.findHistory(studentId); }

    public List<AcademicRecord> findClassRecords(Integer subjectId, int year, AcademicPeriod period) { return service.findClassRecords(subjectId,year,period); }

    public Optional<BigDecimal> subjectAverage(Integer studentId, Integer subjectId, int year) { return service.subjectAverage(studentId,subjectId,year); }

    public Optional<BigDecimal> studentPeriodAverage(Integer studentId, int year, AcademicPeriod period) { return service.studentPeriodAverage(studentId,year,period); }

    public Optional<BigDecimal> classAverage(Integer subjectId, int year, AcademicPeriod period, String section) { return service.classAverage(subjectId,year,period,section); }

    public int saveAttendance(Attendance value, AcademicPeriod period) { return service.saveAttendance(value,period); }

    public Attendance getAttendance(Integer id) { return service.getAttendance(id); }

    public List<Attendance> findAttendance(Integer studentId, LocalDate from, LocalDate to) { return service.findAttendance(studentId,from,to); }

    public List<Attendance> findClassAttendance(Integer subjectId, LocalDate date) { return service.findClassAttendance(subjectId,date); }

    public List<AbsenceAlert> weeklyAbsenceAlerts(LocalDate start, int threshold) { return service.weeklyAbsenceAlerts(start,threshold); }
}
