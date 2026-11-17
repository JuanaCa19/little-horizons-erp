package view;

import controller.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.*;
import static util.ConsoleUI.*;
import static util.InputReader.*;
import static view.ViewSupport.*;

public class AcademicView {
    private final AcademicController controller;
    private final StudentController students;
    private final TeacherController teachers;
    private final SubjectController subjects;
    private final SubjectView subjectView;
    public AcademicView() { this(new AcademicController(),new StudentController(),new TeacherController(),new SubjectController()); }
    public AcademicView(AcademicController controller,StudentController students,TeacherController teachers,SubjectController subjects) {
        this.controller=Objects.requireNonNull(controller);this.students=Objects.requireNonNull(students);
        this.teachers=Objects.requireNonNull(teachers);this.subjects=Objects.requireNonNull(subjects);
        this.subjectView=new SubjectView(subjects,new GradeView());
    }
    public void start() {
        menu("Gestion Academica","Volver al menu principal",new String[]{"Materias","Inscripciones por periodo","Calificaciones y promedios","Asistencia e inasistencias"},
                subjectView::start,this::enrollmentMenu,this::recordMenu,this::attendanceMenu);
    }
    private int student() {
        Student value=students.getById(readId("ID del estudiante"));field("Estudiante",value.getFullName());return value.getId();
    }
    private int teacher() {
        Teacher value=teachers.getById(readId("ID del profesor"));field("Profesor",value.getFullName());return value.getId();
    }
    private int subject() { return subjectView.select().getId(); }
    private int year() { return readInt("Anio academico: ",1900,9999); }
    private AcademicPeriod period(boolean allowFinal) {
        AcademicPeriod[] values=Arrays.stream(AcademicPeriod.values()).filter(value -> allowFinal || value!=AcademicPeriod.FINAL).toArray(AcademicPeriod[]::new);
        return choose("Periodo academico",values,AcademicPeriod::getDbValue,null);
    }
    private void enrollmentMenu() {
        menu("Inscripciones por Periodo","Volver",new String[]{"Inscribir estudiante","Consultar por estudiante y anio","Consultar inscritos de una materia","Cambiar estado"},
                action(this::enroll),action(() -> showEnrollments(controller.findEnrollments(student(),year()))),
                action(() -> showEnrollments(controller.findClassEnrollments(subject(),year(),period(false)))),action(this::enrollmentStatus));
    }
    private void enroll() {
        SubjectEnrollment value=new SubjectEnrollment();value.setStudentId(student());value.setSubjectId(subject());value.setAcademicYear(year());
        value.setPeriod(period(false));value.setSection(readRequiredText("Seccion (ej: A): "));
        value.setEnrollmentDate(readDate("Fecha de inscripcion (dd/MM/yyyy): "));value.setStatus(EnrollmentStatus.ACTIVE);
        showEnrollments(List.of(value));
        if(readYesNo("Guardar inscripcion")) printSuccess("Inscripcion creada. ID: "+controller.enroll(value));else printInfo("No se guardaron cambios.");
    }
    private void enrollmentStatus() {
        SubjectEnrollment value=controller.getEnrollment(readId("ID de inscripcion"));showEnrollments(List.of(value));
        EnrollmentStatus status=choose("Nuevo estado",EnrollmentStatus.values(),EnrollmentStatus::getDbValue,null);
        if(readYesNo("Confirmar cambio de estado")) { controller.updateEnrollmentStatus(value.getId(),status);printSuccess("Inscripcion actualizada."); } else printInfo("No se guardaron cambios.");
    }
    private void showEnrollments(List<SubjectEnrollment> values) {
        printTable(List.of("ID","Estudiante ID","Materia","Anio","Periodo","Seccion","Fecha","Estado"),values.stream().map(value -> row(value.getId(),value.getStudentId(),
                subjects.getById(value.getSubjectId()).getName(),value.getAcademicYear(),value.getPeriod().getDbValue(),value.getSection(),value.getEnrollmentDate(),value.getStatus().getDbValue())).toList());
    }
    private void recordMenu() {
        menu("Calificaciones y Promedios","Volver",new String[]{"Registrar nota","Corregir nota","Historial del estudiante","Notas de una materia y periodo",
                "Promedio del estudiante por materia","Promedio del estudiante por periodo","Promedio del curso por materia"},
                action(() -> saveRecord(null)),action(() -> saveRecord(controller.getRecord(readId("ID de nota")))),
                action(() -> showRecords(controller.findHistory(student()))),action(() -> showRecords(controller.findClassRecords(subject(),year(),period(true)))),
                action(this::subjectAverage),action(this::periodAverage),action(this::classAverage));
    }
    private void saveRecord(AcademicRecord current) {
        AcademicRecord value=new AcademicRecord();
        if(current==null) {
            value.setStudentId(student());value.setSubjectId(subject());value.setAcademicYear(year());value.setPeriod(period(true));
        } else {
            showRecords(List.of(current));value.setId(current.getId());value.setStudentId(current.getStudentId());value.setSubjectId(current.getSubjectId());
            value.setAcademicYear(current.getAcademicYear());value.setPeriod(current.getPeriod());
        }
        value.setTeacherId(teacher());
        value.setGrade(readDecimal("Nota (0.0 a 5.0): ",BigDecimal.ZERO,new BigDecimal("5.0"),1));
        value.setNotes(optional("Observaciones",current==null ? null : current.getNotes()));
        value.setRecordDate(date("Fecha de registro",current==null ? null : current.getRecordDate()));
        showRecords(List.of(value));
        if(readYesNo("Guardar nota")) printSuccess("Nota guardada. ID: "+controller.saveRecord(value));else printInfo("No se guardaron cambios.");
    }
    private void showRecords(List<AcademicRecord> values) {
        printTable(List.of("ID","Estudiante ID","Materia","Profesor ID","Anio","Periodo","Nota","Fecha","Observaciones"),values.stream().map(value -> row(value.getId(),value.getStudentId(),
                subjects.getById(value.getSubjectId()).getName(),value.getTeacherId(),value.getAcademicYear(),value.getPeriod().getDbValue(),value.getGrade(),value.getRecordDate(),value.getNotes())).toList());
    }
    private void showAverage(Optional<BigDecimal> average) {
        average.ifPresentOrElse(value -> field("Promedio",value),() -> printInfo("No hay notas registradas para calcular el promedio."));
    }
    private void subjectAverage() {
        printInfo("Se usa la nota Final si existe; de lo contrario, el promedio de los periodos registrados.");
        showAverage(controller.subjectAverage(student(),subject(),year()));
    }
    private void periodAverage() { showAverage(controller.studentPeriodAverage(student(),year(),period(true))); }
    private void classAverage() {
        int subjectId=subject();int year=year();AcademicPeriod period=period(true);
        String section=readOptionalText("Seccion (Enter para todo el grado): ");showAverage(controller.classAverage(subjectId,year,period,section));
    }
    private void attendanceMenu() {
        menu("Asistencia e Inasistencias","Volver",new String[]{"Registrar asistencia","Corregir asistencia","Consultar por estudiante y fechas","Asistencia de materia por fecha","Alertas de inasistencia semanal"},
                action(() -> saveAttendance(null)),action(() -> saveAttendance(controller.getAttendance(readId("ID de asistencia")))),
                action(this::studentAttendance),action(() -> showAttendance(controller.findClassAttendance(subject(),readDate("Fecha (dd/MM/yyyy): ")))),action(this::absenceAlerts));
    }
    private void saveAttendance(Attendance current) {
        Attendance value=new Attendance();
        if(current==null) { value.setStudentId(student());value.setSubjectId(subject());value.setDate(readDate("Fecha de asistencia (dd/MM/yyyy): ")); }
        else {
            showAttendance(List.of(current));value.setId(current.getId());value.setStudentId(current.getStudentId());value.setSubjectId(current.getSubjectId());value.setDate(current.getDate());
        }
        AcademicPeriod period=period(false);value.setTeacherId(teacher());
        value.setStatus(choose("Estado de asistencia",AttendanceStatus.values(),AttendanceStatus::getDbValue,null));
        value.setNotes(optional("Observaciones",current==null ? null : current.getNotes()));showAttendance(List.of(value));
        if(readYesNo("Guardar asistencia")) printSuccess("Asistencia guardada. ID: "+controller.saveAttendance(value,period));else printInfo("No se guardaron cambios.");
    }
    private void showAttendance(List<Attendance> values) {
        printTable(List.of("ID","Estudiante ID","Materia","Fecha","Estado","Profesor ID","Observaciones"),values.stream().map(value -> row(value.getId(),value.getStudentId(),
                subjects.getById(value.getSubjectId()).getName(),value.getDate(),value.getStatus().getDbValue(),value.getTeacherId(),value.getNotes())).toList());
    }
    private void studentAttendance() {
        int studentId=student();LocalDate from=readDate("Desde (dd/MM/yyyy): ");LocalDate to=readDate("Hasta (dd/MM/yyyy): ");
        showAttendance(controller.findAttendance(studentId,from,to));
    }
    private void absenceAlerts() {
        LocalDate start=readDate("Inicio de la semana (dd/MM/yyyy): ");int threshold=readInt("Reportar mas de cuantos dias de ausencia: ",0,7);
        field("Fin de la semana",start.plusDays(6));
        printInfo("Cada dia se cuenta una vez aunque haya ausencias en varias materias. Las excusas medicas no se cuentan como ausencias.");
        printTable(List.of("Estudiante ID","Nombre","Dias ausente"),controller.weeklyAbsenceAlerts(start,threshold).stream().map(value -> row(value.studentId(),value.studentName(),value.absenceDays())).toList());
    }
}
