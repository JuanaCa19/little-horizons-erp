package view;

import controller.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.*;
import util.exception.ValidationException;

/** Launch each scenario in a fresh JVM to isolate the shared console Scanner. */
public class AcademicViewFlowTest {
    private static void check(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
    public static void main(String[] args) {
        String scenario=args[0];int[] writes={0};
        Student student=new Student();student.setId(1);student.setFirstName("Ana");student.setFirstLastName("Perez");
        Teacher teacher=new Teacher();teacher.setId(1);teacher.setFirstName("Maria");teacher.setFirstLastName("Gil");
        Subject subject=new Subject();subject.setId(1);subject.setCode("MAT-1");subject.setName("Matematicas");subject.setGradeId(1);subject.setWeeklyHours(5);subject.setActive(true);
        AcademicRecord original=new AcademicRecord();original.setId(4);original.setStudentId(1);original.setSubjectId(1);original.setTeacherId(1);original.setAcademicYear(2026);
        original.setPeriod(AcademicPeriod.FIRST);original.setGrade(new BigDecimal("3.0"));original.setRecordDate(LocalDate.of(2026,10,30));
        AcademicController academic=new AcademicController() {
            @Override public int enroll(SubjectEnrollment value) {
                check(value.getStudentId()==1 && value.getSubjectId()==1 && value.getPeriod()==AcademicPeriod.FIRST && value.getAcademicYear()==2026,"Enrollment form");writes[0]++;return 10;
            }
            @Override public int saveRecord(AcademicRecord value) {
                check(value.getGrade().compareTo(new BigDecimal("4.5"))==0 && value.getTeacherId()==1,"Grade form or decimal retry");writes[0]++;return 20;
            }
            @Override public AcademicRecord getRecord(Integer id) { return original; }
            @Override public List<AcademicRecord> findHistory(Integer id) {
                if(scenario.equals("validation-error")) throw new ValidationException("Academic failure");return List.of(original);
            }
            @Override public int saveAttendance(Attendance value,AcademicPeriod period) {
                check(value.getStatus()==AttendanceStatus.MEDICAL_EXCUSE && period==AcademicPeriod.FOURTH && value.getTeacherId()==1,"Attendance form");writes[0]++;return 30;
            }
            @Override public Optional<BigDecimal> subjectAverage(Integer studentId,Integer subjectId,int year) { return Optional.empty(); }
            @Override public List<AbsenceAlert> weeklyAbsenceAlerts(LocalDate start,int threshold) { return List.of(new AbsenceAlert(1,"Ana Perez",4)); }
        };
        SubjectController subjects=new SubjectController() {
            @Override public Subject getById(Integer id) { return subject; }
            @Override public List<Subject> findAll() { return List.of(subject); }
            @Override public int create(Subject value) {
                check(value.getCode().equals("CIE-1") && value.getGradeId()==1 && value.getWeeklyHours()==3,"Subject form");writes[0]++;return 40;
            }
        };
        StudentController students=new StudentController() { @Override public Student getById(Integer id) { return student; } };
        TeacherController teachers=new TeacherController() { @Override public Teacher getById(Integer id) { return teacher; } };
        AcademicView view=new AcademicView(academic,students,teachers,subjects);
        GradeController grades=new GradeController() {
            @Override public List<Grade> findAll() { Grade value=new Grade();value.setId(1);value.setName("Primero");return List.of(value); }
        };
        String note="3\n1\n1\n1\n2026\n1\n1\n5.1\n3.45\nwrong\n4,5\n\n30/10/2026\n";
        String input=switch(scenario) {
            case "navigation" -> "2\n2\n5\n3\n8\n4\n6\n5\n8\n";
            case "enroll" -> "2\n1\n1\n1\n2026\n1\nA\n20/01/2026\nsi\n\n5\n5\n";
            case "note" -> note+"si\n\n8\n5\n";
            case "decline-note" -> note+"no\n\n8\n5\n";
            case "cancel-note" -> "3\n1\n1\ncancelar\n8\n5\n";
            case "cancel-correction" -> "3\n2\n4\n1\n4.5\ncancelar\n8\n5\n";
            case "attendance" -> "4\n1\n1\n1\n19/10/2026\n4\n1\n4\nSoporte medico\nsi\n\n6\n5\n";
            case "alerts" -> "4\n5\n19/10/2026\n3\n\n6\n5\n";
            case "empty-average" -> "3\n5\n1\n1\n2026\n\n8\n5\n";
            case "history", "validation-error" -> "3\n3\n1\n\n8\n5\n";
            case "subject-create" -> "1\nCIE-1\nCiencias\n\n1\n3\nsi\nsi\n\n6\n";
            default -> throw new IllegalArgumentException(scenario);
        };
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream output=new ByteArrayOutputStream();PrintStream console=System.out,errors=System.err;
        try(PrintStream capture=new PrintStream(output,true,StandardCharsets.UTF_8)) {
            System.setOut(capture);System.setErr(capture);
            if(scenario.equals("navigation")) new MainMenuView(new StudentView(),new TeacherView(),view).start();
            else if(scenario.equals("subject-create")) new SubjectView(subjects,new GradeView(grades)).start();
            else view.start();
        } finally { System.setOut(console);System.setErr(errors); }
        String text=output.toString(StandardCharsets.UTF_8);
        check(writes[0]==(Set.of("enroll","note","attendance","subject-create").contains(scenario)?1:0),"Unexpected writes");
        check(original.getGrade().compareTo(new BigDecimal("3.0"))==0,"Cancelled correction changed original grade");
        if(scenario.startsWith("cancel")) check(text.contains("Operacion cancelada"),"Cancellation message missing");
        if(scenario.equals("note")) check(text.contains("Ingrese un valor") && text.contains("Nota guardada"),"Decimal validation missing");
        if(scenario.equals("alerts")) check(text.contains("Ana Perez"),"Absence report missing");
        if(scenario.equals("empty-average")) check(text.contains("No hay notas"),"Empty average must not show zero");
        if(scenario.equals("validation-error")) check(text.contains("Academic failure"),"Validation not displayed");
        if(scenario.equals("history")) check(text.contains("Matematicas") && text.contains("3.0"),"History not shown");
        if(scenario.equals("navigation")) check(text.contains("Hasta pronto"),"Main menu exit failed");
        System.out.println("Passed academic view scenario: "+scenario);
    }
}
