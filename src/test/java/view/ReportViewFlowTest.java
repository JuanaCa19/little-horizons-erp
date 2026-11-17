package view;

import controller.ReportController;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import model.AbsenceAlert;
import model.enums.*;
import model.report.*;
import util.exception.DataAccessException;

/** Run each scenario in a fresh JVM because InputReader uses a shared scanner. */
public class ReportViewFlowTest {
    private static void check(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
    public static void main(String[] args) {
        String scenario=args[0];LocalDate date=LocalDate.of(2026,11,16);int[] reads={0};
        ReportController reports=new ReportController() {
            @Override public SchoolOverview overview(LocalDate reference) {
                reads[0]++;if(scenario.equals("database-error")) throw new DataAccessException("Report database failure");return new SchoolOverview(40,38,35,30,50,20,10,3);
            }
            @Override public List<GradeEnrollmentCount> enrollmentByGrade(int year) { reads[0]++;return List.of(new GradeEnrollmentCount(1,"Primero",5,3,1,1)); }
            @Override public List<StudentAverageReport> studentAverages(int year,AcademicPeriod period) { reads[0]++;return scenario.equals("empty") ? List.of() : List.of(new StudentAverageReport(1,"Ana Perez","Primero",new BigDecimal("4.25"),2)); }
            @Override public List<StudentAverageReport> studentsBelowAverage(int year,AcademicPeriod period,BigDecimal threshold) {
                check(threshold.compareTo(new BigDecimal("3.00"))==0,"Support threshold");reads[0]++;return List.of(new StudentAverageReport(2,"Luis Ruiz","Primero",new BigDecimal("2.50"),2));
            }
            @Override public List<SubjectAverageReport> subjectAverages(int year,AcademicPeriod period) { reads[0]++;return List.of(new SubjectAverageReport(1,"Matematicas","Primero","A",new BigDecimal("4.25"),5)); }
            @Override public List<TeacherWorkloadReport> teacherWorkload(int year) { reads[0]++;return List.of(new TeacherWorkloadReport(1,"Maria Gil","Activo",2,150)); }
            @Override public List<AttendanceStatistics> attendance(LocalDate from,LocalDate to) { reads[0]++;return List.of(new AttendanceStatistics(1,"Ana Perez",1,1,1,1)); }
            @Override public List<AbsenceAlert> weeklyAbsenceAlerts(LocalDate start,int threshold) { check(threshold==3,"Weekly threshold");reads[0]++;return List.of(new AbsenceAlert(1,"Ana Perez",4)); }
            @Override public List<IncomeReport> income(LocalDate from,LocalDate to) { reads[0]++;return List.of(new IncomeReport(1,"Uniforme",2,new BigDecimal("357.00"))); }
            @Override public List<DebtReport> debts(LocalDate reference,boolean overdueOnly) {
                check(overdueOnly==scenario.equals("overdue-debt"),"Debt mode");reads[0]++;return List.of(new DebtReport(1,"Ana Perez",3,new BigDecimal("250.00"),1,new BigDecimal("100.00"),date.minusMonths(2)));
            }
            @Override public List<ClubOccupancyReport> clubOccupancy() { reads[0]++;return List.of(new ClubOccupancyReport(1,"Robotica",true,3,2,2)); }
            @Override public List<EventParticipationReport> eventParticipation(LocalDate from,LocalDate to) { reads[0]++;return List.of(new EventParticipationReport(1,"Feria",date,"Cientifico",true,5,3,2)); }
            @Override public List<StudentParticipationReport> studentParticipation() { reads[0]++;return List.of(new StudentParticipationReport(1,"Ana Perez",2,3,4)); }
            @Override public List<ResourceInventoryReport> inventory() { reads[0]++;return List.of(new ResourceInventoryReport(1,"Libros",5,2,1,1,1)); }
            @Override public List<OverdueLoanReport> overdueLoans(LocalDate reference) { reads[0]++;return List.of(new OverdueLoanReport(1,BorrowerType.TEACHER,1,"Maria Gil",1,"Libro",date.minusDays(2),2)); }
        };
        ReportView view=new ReportView(reports);
        String input=switch(scenario) {
            case "navigation" -> "7\n2\n5\n4\n3\n5\n4\n6\n4\n7\n3\n8\n8\n";
            case "overview", "database-error" -> "1\n16/11/2026\n\n8\n";
            case "enrollment" -> "2\n1\n2026\n\n5\n8\n";
            case "averages", "empty" -> "2\n2\n2026\n1\n\n5\n8\n";
            case "support" -> "2\n3\n2026\n1\n3,00\n\n5\n8\n";
            case "subjects" -> "2\n4\n2026\n1\n\n5\n8\n";
            case "workload" -> "3\n2026\n\n8\n";
            case "attendance" -> "4\n1\n01/11/2026\n16/11/2026\n\n3\n8\n";
            case "weekly" -> "4\n2\n09/11/2026\n3\n\n3\n8\n";
            case "income" -> "5\n1\n01/11/2026\n16/11/2026\n\n4\n8\n";
            case "debt" -> "5\n2\n16/11/2026\n\n4\n8\n";
            case "overdue-debt" -> "5\n3\n16/11/2026\n\n4\n8\n";
            case "clubs" -> "6\n1\n\n4\n8\n";
            case "events" -> "6\n2\n01/11/2026\n16/11/2026\n\n4\n8\n";
            case "participation" -> "6\n3\n\n4\n8\n";
            case "inventory" -> "7\n1\n\n3\n8\n";
            case "loans" -> "7\n2\n16/11/2026\n\n3\n8\n";
            case "cancel" -> "2\n2\ncancelar\n5\n8\n";
            default -> throw new IllegalArgumentException(scenario);
        };
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream output=new ByteArrayOutputStream();PrintStream console=System.out,errors=System.err;
        try(PrintStream capture=new PrintStream(output,true,StandardCharsets.UTF_8)) {
            System.setOut(capture);System.setErr(capture);
            if(scenario.equals("navigation")) new MainMenuView(new StudentView(),new TeacherView(),new AcademicView(),new ExtracurricularView(),new FinanceView(),new ResourceView(),view).start();else view.start();
        } finally { System.setOut(console);System.setErr(errors); }
        String text=output.toString(StandardCharsets.UTF_8);
        check(reads[0]==(Set.of("navigation","cancel").contains(scenario)?0:1),"Unexpected report execution count");
        String expected=switch(scenario) {
            case "navigation" -> "Hasta pronto";case "overview" -> "Estudiantes registrados";case "database-error" -> "Report database failure";
            case "enrollment" -> "Primero";case "averages", "subjects" -> "4.25";case "empty" -> "No se encontraron resultados";
            case "support" -> "Luis Ruiz";case "workload" -> "2.50";case "attendance" -> "66.67";case "weekly" -> "Ana Perez";
            case "income" -> "357.00";case "debt", "overdue-debt" -> "250.00";case "clubs" -> "66.67";case "events" -> "Feria";
            case "participation" -> "Ana Perez";case "inventory" -> "Libros";case "loans" -> "Maria Gil";case "cancel" -> "Operacion cancelada";default -> "";
        };
        check(text.contains(expected),"Report output missing: "+expected);
        System.out.println("Passed report view scenario: "+scenario);
    }
}
