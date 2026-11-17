package service;

import dao.ReportDAO;
import dao.impl.ReportDAOImpl;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import model.AbsenceAlert;
import model.enums.AcademicPeriod;
import model.report.*;
import util.ConnectionDB;
import static service.ServiceValidation.*;

public class ReportService {
    private final ReportDAO reports;
    private final TransactionManager transactions;
    public ReportService() { this(new ReportDAOImpl(),ConnectionDB::getConnection); }
    public ReportService(ReportDAO reports,Supplier<Connection> connections) { this.reports=Objects.requireNonNull(reports);this.transactions=new TransactionManager(connections); }
    private void date(LocalDate date) { require(date!=null,"La fecha es obligatoria."); }
    private void range(LocalDate from,LocalDate to) { date(from);date(to);require(!to.isBefore(from),"La fecha final no puede ser anterior a la inicial."); }
    private void period(int year,AcademicPeriod period) { ServiceValidation.year(year);require(period!=null,"El periodo es obligatorio."); }
    public SchoolOverview overview(LocalDate date) { date(date);return transactions.execute(c -> reports.overview(date,c)); }
    public List<GradeEnrollmentCount> enrollmentByGrade(int year) { ServiceValidation.year(year);return transactions.execute(c -> reports.enrollmentByGrade(year,c)); }
    public List<StudentAverageReport> studentAverages(int year,AcademicPeriod period) { period(year,period);return transactions.execute(c -> reports.studentAverages(year,period,c)); }
    public List<StudentAverageReport> studentsBelowAverage(int year,AcademicPeriod period,BigDecimal threshold) {
        require(threshold!=null && threshold.signum()>=0 && threshold.compareTo(new BigDecimal("5.0"))<=0,"El umbral debe estar entre 0.0 y 5.0.");
        return studentAverages(year,period).stream().filter(value -> value.average().compareTo(threshold)<0).toList();
    }
    public List<SubjectAverageReport> subjectAverages(int year,AcademicPeriod period) { period(year,period);return transactions.execute(c -> reports.subjectAverages(year,period,c)); }
    public List<TeacherWorkloadReport> teacherWorkload(int year) { ServiceValidation.year(year);return transactions.execute(c -> reports.teacherWorkload(year,c)); }
    public List<AttendanceStatistics> attendance(LocalDate from,LocalDate to) { range(from,to);return transactions.execute(c -> reports.attendance(from,to,c)); }
    public List<AbsenceAlert> weeklyAbsenceAlerts(LocalDate start,int threshold) {
        date(start);require(threshold>=0 && threshold<=7,"El umbral debe estar entre 0 y 7 dias.");
        require(!start.isAfter(LocalDate.MAX.minusDays(6)),"La semana excede el rango de fechas.");
        return transactions.execute(c -> reports.absenceAlerts(start,start.plusDays(6),threshold,c));
    }
    public List<IncomeReport> income(LocalDate from,LocalDate to) { range(from,to);return transactions.execute(c -> reports.income(from,to,c)); }
    public List<DebtReport> debts(LocalDate referenceDate,boolean overdueOnly) {
        date(referenceDate);
        require(!referenceDate.isBefore(LocalDate.MIN.plusMonths(1)),"La fecha de corte esta fuera del rango permitido.");
        List<DebtReport> values=transactions.execute(c -> reports.debts(referenceDate.minusMonths(1),c));
        return overdueOnly ? values.stream().filter(value -> value.overdueCharges()>0).toList() : values;
    }
    public List<ClubOccupancyReport> clubOccupancy() { return transactions.execute(reports::clubOccupancy); }
    public List<EventParticipationReport> eventParticipation(LocalDate from,LocalDate to) { range(from,to);return transactions.execute(c -> reports.eventParticipation(from,to,c)); }
    public List<StudentParticipationReport> studentParticipation() { return transactions.execute(reports::studentParticipation); }
    public List<ResourceInventoryReport> inventory() { return transactions.execute(reports::inventory); }
    public List<OverdueLoanReport> overdueLoans(LocalDate date) { date(date);return transactions.execute(c -> reports.overdueLoans(date,c)); }
}
