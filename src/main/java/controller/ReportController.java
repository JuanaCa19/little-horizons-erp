package controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import model.AbsenceAlert;
import model.enums.AcademicPeriod;
import model.report.*;
import service.ReportService;

public class ReportController {
    private final ReportService service;
    public ReportController() { this(new ReportService()); }
    public ReportController(ReportService service) { this.service=Objects.requireNonNull(service); }

    public SchoolOverview overview(LocalDate date) { return service.overview(date); }

    public List<GradeEnrollmentCount> enrollmentByGrade(int year) { return service.enrollmentByGrade(year); }

    public List<StudentAverageReport> studentAverages(int year,AcademicPeriod period) { return service.studentAverages(year,period); }

    public List<StudentAverageReport> studentsBelowAverage(int year,AcademicPeriod period,BigDecimal threshold) { return service.studentsBelowAverage(year,period,threshold); }

    public List<SubjectAverageReport> subjectAverages(int year,AcademicPeriod period) { return service.subjectAverages(year,period); }

    public List<TeacherWorkloadReport> teacherWorkload(int year) { return service.teacherWorkload(year); }

    public List<AttendanceStatistics> attendance(LocalDate from,LocalDate to) { return service.attendance(from,to); }

    public List<AbsenceAlert> weeklyAbsenceAlerts(LocalDate start,int threshold) { return service.weeklyAbsenceAlerts(start,threshold); }

    public List<IncomeReport> income(LocalDate from,LocalDate to) { return service.income(from,to); }

    public List<DebtReport> debts(LocalDate date,boolean overdueOnly) { return service.debts(date,overdueOnly); }

    public List<ClubOccupancyReport> clubOccupancy() { return service.clubOccupancy(); }

    public List<EventParticipationReport> eventParticipation(LocalDate from,LocalDate to) { return service.eventParticipation(from,to); }

    public List<StudentParticipationReport> studentParticipation() { return service.studentParticipation(); }

    public List<ResourceInventoryReport> inventory() { return service.inventory(); }

    public List<OverdueLoanReport> overdueLoans(LocalDate date) { return service.overdueLoans(date); }
}
