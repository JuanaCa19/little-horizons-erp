package view;

import controller.ReportController;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import model.enums.AcademicPeriod;
import model.report.*;
import static util.ConsoleUI.*;
import static util.InputReader.*;
import static view.ViewSupport.*;

public class ReportView {
    private final ReportController controller;
    public ReportView() { this(new ReportController()); }
    public ReportView(ReportController controller) { this.controller=Objects.requireNonNull(controller); }
    public void start() {
        menu("Reportes y Estadisticas","Volver al menu principal",new String[]{"Resumen general","Matricula y rendimiento academico","Carga docente","Asistencia e inasistencias",
                "Recaudos y cartera","Participacion extracurricular","Inventario y prestamos"},action(this::overview),this::academicMenu,action(this::teacherWorkload),
                this::attendanceMenu,this::financeMenu,this::extracurricularMenu,this::resourceMenu);
    }
    private int year() { return readInt("Anio academico: ",1900,9999); }
    private AcademicPeriod period() { return choose("Periodo",AcademicPeriod.values(),AcademicPeriod::getDbValue,null); }
    private LocalDate date(String label) { return readDate(label+" (dd/MM/yyyy): "); }
    private void overview() {
        LocalDate date=date("Fecha de referencia para vencidos");SchoolOverview value=controller.overview(date);
        printHeader("Resumen general del colegio");field("Estudiantes registrados",value.totalStudents());field("Estudiantes activos",value.activeStudents());field("Profesores activos",value.activeTeachers());
        field("Clubes activos",value.activeClubs());field("Recursos registrados",value.totalResources());field("Recursos disponibles",value.availableResources());
        field("Prestamos abiertos",value.openLoans());field("Prestamos abiertos vencidos",value.overdueLoans());
    }
    private void academicMenu() {
        menu("Reportes Academicos","Volver",new String[]{"Matricula por grado","Promedios de estudiantes","Estudiantes que requieren apoyo","Promedios por materia y seccion"},
                action(this::enrollments),action(() -> showStudentAverages(controller.studentAverages(year(),period()))),action(this::support),action(this::subjectAverages));
    }
    private void enrollments() {
        printTable(List.of("Grado ID","Grado","Total","Activas","Completadas","Retiradas"),controller.enrollmentByGrade(year()).stream()
                .map(value -> row(value.gradeId(),value.gradeName(),value.total(),value.active(),value.completed(),value.withdrawn())).toList());
    }
    private void showStudentAverages(List<StudentAverageReport> values) {
        printInfo("Promedios del periodo seleccionado, solo con notas registradas. Final se consulta por separado.");
        printTable(List.of("Estudiante ID","Estudiante","Grado","Materias con nota","Promedio"),values.stream().map(value -> row(value.studentId(),value.studentName(),value.gradeName(),value.gradedSubjects(),value.average())).toList());
    }
    private void support() {
        int year=year();AcademicPeriod period=period();BigDecimal threshold=readDecimal("Promedio inferior a: ",BigDecimal.ZERO,new BigDecimal("5.0"),2);
        showStudentAverages(controller.studentsBelowAverage(year,period,threshold));
    }
    private void subjectAverages() {
        printTable(List.of("Materia ID","Materia","Grado","Seccion","Estudiantes con nota","Promedio"),controller.subjectAverages(year(),period()).stream()
                .map(value -> row(value.subjectId(),value.subjectName(),value.gradeName(),value.section(),value.gradedStudents(),value.average())).toList());
    }
    private void teacherWorkload() {
        printInfo("Horas semanales segun los horarios registrados; profesores sin asignaciones tambien se muestran.");
        printTable(List.of("Profesor ID","Profesor","Estado","Asignaciones","Horas programadas"),controller.teacherWorkload(year()).stream()
                .map(value -> row(value.teacherId(),value.teacherName(),value.status(),value.assignments(),value.scheduledHours())).toList());
    }
    private void attendanceMenu() {
        menu("Reportes de Asistencia","Volver",new String[]{"Resumen por fechas","Alertas semanales"},action(this::attendance),action(this::absenceAlerts));
    }
    private void attendance() {
        LocalDate from=date("Desde"),to=date("Hasta");
        printInfo("Asistencia % = (presente + tardanza) / registros sin excusa medica. Sin base de calculo se muestra -.");
        printTable(List.of("Estudiante ID","Estudiante","Presente","Ausente","Tardanza","Excusa","Registros","Asistencia %"),controller.attendance(from,to).stream()
                .map(value -> row(value.studentId(),value.studentName(),value.present(),value.absent(),value.late(),value.excused(),value.total(),value.attendanceRate())).toList());
    }
    private void absenceAlerts() {
        LocalDate from=date("Inicio de la semana");int threshold=readInt("Reportar mas de cuantos dias de ausencia: ",0,7);
        printInfo("Cada dia se cuenta una vez; no se incluyen excusas medicas.");
        printTable(List.of("Estudiante ID","Estudiante","Dias de ausencia"),controller.weeklyAbsenceAlerts(from,threshold).stream().map(value -> row(value.studentId(),value.studentName(),value.absenceDays())).toList());
    }
    private void financeMenu() {
        menu("Reportes Financieros","Volver",new String[]{"Recaudos por concepto","Cartera pendiente","Morosos con mas de un mes de atraso"},action(this::income),action(() -> debts(false)),action(() -> debts(true)));
    }
    private void income() {
        LocalDate from=date("Desde"),to=date("Hasta");List<IncomeReport> values=controller.income(from,to);
        printTable(List.of("Concepto ID","Concepto","Pagos","Total recibido COP"),values.stream().map(value -> row(value.conceptId(),value.conceptName(),value.payments(),value.total())).toList());
        field("Total recibido COP (IVA incluido)",values.stream().map(IncomeReport::total).reduce(new BigDecimal("0.00"),BigDecimal::add));
    }
    private void debts(boolean overdueOnly) {
        LocalDate reference=date("Fecha de corte para mora");List<DebtReport> values=controller.debts(reference,overdueOnly);
        printInfo("Cartera actual en valores base sin IVA. Mora: vencimiento anterior a la fecha de corte menos un mes. Se excluyen cargos de valor cero.");
        printTable(List.of("Estudiante ID","Estudiante","Pendientes","Base pendiente COP","En mora","Base en mora COP","Vence mas antiguo"),values.stream()
                .map(value -> row(value.studentId(),value.studentName(),value.pendingCharges(),value.pendingBase(),value.overdueCharges(),value.overdueBase(),value.earliestDueDate())).toList());
    }
    private void extracurricularMenu() {
        menu("Reportes Extracurriculares","Volver",new String[]{"Ocupacion de clubes","Participacion en eventos","Participacion por estudiante"},action(this::clubs),action(this::events),action(this::studentParticipation));
    }
    private void clubs() {
        printTable(List.of("Club ID","Club","Activo","Cupo","Miembros activos","Disponibles","Ocupacion %","Estudiantes historicos"),controller.clubOccupancy().stream()
                .map(value -> row(value.clubId(),value.clubName(),value.active(),value.capacity(),value.activeMembers(),value.availableSeats(),value.occupancyRate(),value.historicalStudents())).toList());
    }
    private void events() {
        LocalDate from=date("Desde"),to=date("Hasta");
        printTable(List.of("Evento ID","Evento","Fecha","Tipo","Activo","Participantes","Competidores","Premiados"),controller.eventParticipation(from,to).stream()
                .map(value -> row(value.eventId(),value.eventName(),value.date(),value.type(),value.active(),value.participants(),value.competitors(),value.awards())).toList());
    }
    private void studentParticipation() {
        printTable(List.of("Estudiante ID","Estudiante","Clubes activos","Clubes historicos","Eventos"),controller.studentParticipation().stream()
                .map(value -> row(value.studentId(),value.studentName(),value.activeClubs(),value.historicalClubs(),value.events())).toList());
    }
    private void resourceMenu() {
        menu("Reportes de Recursos","Volver",new String[]{"Inventario por categoria","Prestamos abiertos vencidos"},action(this::inventory),action(this::overdueLoans));
    }
    private void inventory() {
        printTable(List.of("Categoria ID","Categoria","Total","Disponibles","Prestados","Mantenimiento","Dados de baja"),controller.inventory().stream()
                .map(value -> row(value.categoryId(),value.categoryName(),value.total(),value.available(),value.loaned(),value.maintenance(),value.retired())).toList());
    }
    private void overdueLoans() {
        printTable(List.of("Prestamo ID","Tipo","Solicitante ID","Solicitante","Recurso ID","Recurso","Debia devolver","Dias vencido"),controller.overdueLoans(date("Fecha de corte")).stream()
                .map(value -> row(value.loanId(),value.borrowerType().getDbValue(),value.borrowerId(),value.borrowerName(),value.resourceId(),value.resourceName(),value.expectedReturnDate(),value.overdueDays())).toList());
    }
}
