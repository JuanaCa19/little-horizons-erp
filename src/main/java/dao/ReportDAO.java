package dao;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import model.enums.AcademicPeriod;
import model.report.*;

public interface ReportDAO {
    List<model.AbsenceAlert> absenceAlerts(LocalDate from,LocalDate to,int threshold,Connection connection);
    SchoolOverview overview(LocalDate date,Connection connection);
    List<GradeEnrollmentCount> enrollmentByGrade(int year,Connection connection);
    List<StudentAverageReport> studentAverages(int year,AcademicPeriod period,Connection connection);
    List<SubjectAverageReport> subjectAverages(int year,AcademicPeriod period,Connection connection);
    List<TeacherWorkloadReport> teacherWorkload(int year,Connection connection);
    List<AttendanceStatistics> attendance(LocalDate from,LocalDate to,Connection connection);
    List<IncomeReport> income(LocalDate from,LocalDate to,Connection connection);
    List<DebtReport> debts(LocalDate cutoff,Connection connection);
    List<ClubOccupancyReport> clubOccupancy(Connection connection);
    List<EventParticipationReport> eventParticipation(LocalDate from,LocalDate to,Connection connection);
    List<StudentParticipationReport> studentParticipation(Connection connection);
    List<ResourceInventoryReport> inventory(Connection connection);
    List<OverdueLoanReport> overdueLoans(LocalDate date,Connection connection);
}
