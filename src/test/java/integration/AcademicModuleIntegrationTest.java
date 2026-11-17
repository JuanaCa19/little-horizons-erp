package integration;

import dao.*;
import dao.impl.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.*;
import service.*;
import util.exception.ValidationException;

public class AcademicModuleIntegrationTest {
    private static int checks;
    private static void check(boolean value,String message) { if(!value) throw new AssertionError(message);checks++; }
    private static void rejected(Runnable work) {
        try { work.run(); } catch(ValidationException e) { checks++;return; }
        throw new AssertionError("Expected validation failure");
    }
    private static SubjectEnrollment enrollment(int studentId,int subjectId,AcademicPeriod period) {
        SubjectEnrollment value=new SubjectEnrollment();value.setStudentId(studentId);value.setSubjectId(subjectId);value.setAcademicYear(2026);
        value.setPeriod(period);value.setSection(" a ");value.setEnrollmentDate(LocalDate.of(2026,1,20));return value;
    }
    private static AcademicRecord record(int studentId,int subjectId,AcademicPeriod period,String grade) {
        AcademicRecord value=new AcademicRecord();value.setStudentId(studentId);value.setSubjectId(subjectId);value.setTeacherId(1);
        value.setAcademicYear(2026);value.setPeriod(period);value.setGrade(new BigDecimal(grade));value.setRecordDate(LocalDate.of(2026,10,30));return value;
    }
    private static Attendance attendance(int subjectId,LocalDate date,AttendanceStatus status) {
        Attendance value=new Attendance();value.setStudentId(1);value.setSubjectId(subjectId);value.setTeacherId(1);value.setDate(date);value.setStatus(status);return value;
    }
    public static void main(String[] args) throws Exception {
        try(TemporaryDatabase db=new TemporaryDatabase("grados","estudiantes","matriculas","materias","profesores","asignaciones_profesor","inscripciones_materia","registros_academicos","asistencias")) {
            db.execute("INSERT INTO grados(id_grado,nombre_grado,nivel_numerico) VALUES(1,'Primero',1),(2,'Segundo',2)");
            db.execute("INSERT INTO estudiantes(id_estudiante,tipo_documento,numero_documento,primer_nombre,primer_apellido,fecha_nacimiento,genero,fecha_ingreso,estado) VALUES (1,'TI','111','Ana','Perez','2015-01-01','F','2025-01-01','Activo'),(2,'TI','222','Luis','Ruiz','2015-02-01','M','2025-01-01','Activo'),(3,'TI','333','Sara','Diaz','2014-01-01','F','2025-01-01','Activo'),(4,'TI','444','Juan','Gil','2014-01-01','M','2025-01-01','Retirado')");
            db.execute("INSERT INTO matriculas(id_estudiante,id_grado,anio_academico,estado_matricula,fecha_matricula) VALUES(1,1,2026,'Activa','2026-01-15'),(2,1,2026,'Activa','2026-01-15'),(3,2,2026,'Activa','2026-01-15')");
            db.execute("INSERT INTO materias(id_materia,codigo_materia,nombre_materia,id_grado,horas_semanales,esta_activa) VALUES(1,'MAT-1','Matematicas',1,5,1),(2,'ESP-1','Castellano',1,4,1),(3,'MAT-2','Matematicas',2,5,1),(4,'OFF-1','Inactiva',1,2,0)");
            db.execute("INSERT INTO profesores(id_profesor,tipo_documento,numero_documento,primer_nombre,primer_apellido,correo_electronico,fecha_contratacion,estado) VALUES(1,'CC','100','Maria','Gil','maria@example.com','2025-01-01','Activo'),(2,'CC','200','Jose','Diaz','jose@example.com','2025-01-01','Activo')");
            db.execute("INSERT INTO asignaciones_profesor(id_profesor,id_materia,anio_academico,seccion) VALUES(1,1,2026,'A'),(1,2,2026,'A'),(2,1,2026,'B')");
            SubjectDAO subjects=new SubjectDAOImpl(db.connections());
            AcademicService service=new AcademicService(subjects,new SubjectEnrollmentDAOImpl(db.connections()),new AcademicRecordDAOImpl(db.connections()),
                    new AttendanceDAOImpl(db.connections()),new AcademicContextDAOImpl(),db.connections());
            GradeDAO grades=new GradeDAO() {
                public Optional<Grade> findById(Integer id) { Grade grade=new Grade();grade.setId(id);return id==1 || id==2 ? Optional.of(grade) : Optional.empty(); }
                public List<Grade> findAll() { return List.of(); }
            };
            SubjectService catalog=new SubjectService(subjects,grades);
            Subject added=new Subject();added.setCode(" cie-1 ");added.setName("Ciencias");added.setGradeId(1);added.setWeeklyHours(3);added.setActive(true);
            int addedId=catalog.create(added);check(added.getCode().equals("CIE-1") && catalog.getById(addedId).getWeeklyHours()==3,"Subject creation and mapping");
            Subject duplicate=new Subject();duplicate.setCode("CIE-1");duplicate.setName("Otra");duplicate.setGradeId(1);duplicate.setWeeklyHours(2);
            rejected(() -> catalog.create(duplicate));duplicate.setCode("OTHER");duplicate.setName("Ciencias");rejected(() -> catalog.create(duplicate));
            duplicate.setName("Otra");duplicate.setWeeklyHours(0);rejected(() -> catalog.create(duplicate));
            added.setGradeId(2);catalog.update(added);check(catalog.getById(addedId).getGradeId()==2,"Unused subject can change grade");
            Subject moved=catalog.getById(1);moved.setGradeId(2);rejected(() -> catalog.update(moved));
            Subject used=catalog.getById(1);used.setActive(false);catalog.update(used);check(!catalog.getById(1).isActive(),"Subject deactivation preserves catalog");used.setActive(true);catalog.update(used);
            rejected(() -> service.enroll(enrollment(3,1,AcademicPeriod.FIRST)));
            rejected(() -> service.enroll(enrollment(4,1,AcademicPeriod.FIRST)));
            rejected(() -> service.enroll(enrollment(1,4,AcademicPeriod.FIRST)));
            rejected(() -> service.enroll(enrollment(1,1,AcademicPeriod.FINAL)));
            SubjectEnrollment first=enrollment(1,1,AcademicPeriod.FIRST);int firstId=service.enroll(first);
            check(first.getStatus()==EnrollmentStatus.ACTIVE && first.getSection().equals("A") && service.getEnrollment(firstId).getPeriod()==AcademicPeriod.FIRST,"Enrollment mapping and normalization");
            rejected(() -> service.enroll(enrollment(1,1,AcademicPeriod.FIRST)));
            SubjectEnrollment early=enrollment(2,1,AcademicPeriod.SECOND);early.setEnrollmentDate(LocalDate.of(2026,1,1));rejected(() -> service.enroll(early));
            for(AcademicPeriod period:List.of(AcademicPeriod.SECOND,AcademicPeriod.THIRD,AcademicPeriod.FOURTH)) service.enroll(enrollment(1,1,period));
            service.enroll(enrollment(1,2,AcademicPeriod.FIRST));service.enroll(enrollment(2,1,AcademicPeriod.FIRST));
            check(service.findEnrollments(1,2026).size()==5,"Student subject enrollments");
            check(service.findClassEnrollments(1,2026,AcademicPeriod.FIRST).size()==2,"Class membership");
            AcademicRecord bad=record(1,1,AcademicPeriod.FIRST,"5.1");rejected(() -> service.saveRecord(bad));
            bad.setGrade(new BigDecimal("-0.1"));rejected(() -> service.saveRecord(bad));
            bad.setGrade(new BigDecimal("3.45"));rejected(() -> service.saveRecord(bad));
            bad.setGrade(new BigDecimal("4.0"));bad.setTeacherId(2);rejected(() -> service.saveRecord(bad));
            rejected(() -> service.saveRecord(record(2,2,AcademicPeriod.FIRST,"4.0")));
            AcademicRecord math=record(1,1,AcademicPeriod.FIRST,"4.0");int gradeId=service.saveRecord(math);
            check(service.getRecord(gradeId).getGrade().compareTo(new BigDecimal("4.0"))==0,"Grade mapping");
            rejected(() -> service.saveRecord(record(1,1,AcademicPeriod.FIRST,"3.0")));
            math.setGrade(new BigDecimal("3.0"));math.setNotes("Correccion");service.saveRecord(math);
            check(service.getRecord(gradeId).getNotes().equals("Correccion"),"Correct existing grade");
            service.saveRecord(record(1,1,AcademicPeriod.SECOND,"4.0"));service.saveRecord(record(1,1,AcademicPeriod.THIRD,"5.0"));
            service.saveRecord(record(1,2,AcademicPeriod.FIRST,"5.0"));service.saveRecord(record(2,1,AcademicPeriod.FIRST,"5.0"));
            check(service.subjectAverage(1,1,2026).orElseThrow().compareTo(new BigDecimal("4.00"))==0,"Period-only subject average");
            check(service.studentPeriodAverage(1,2026,AcademicPeriod.FIRST).orElseThrow().compareTo(new BigDecimal("4.00"))==0,"Student period average");
            check(service.classAverage(1,2026,AcademicPeriod.FIRST,"A").orElseThrow().compareTo(new BigDecimal("4.00"))==0,"Class average and section filter");
            check(service.classAverage(1,2026,AcademicPeriod.FIRST,"B").isEmpty(),"Empty group has no fabricated zero");
            service.saveRecord(record(1,1,AcademicPeriod.FINAL,"4.5"));
            check(service.subjectAverage(1,1,2026).orElseThrow().compareTo(new BigDecimal("4.50"))==0,"Final replaces period average without double counting");
            check(service.subjectAverage(2,2,2026).isEmpty(),"No grades means no average");
            check(service.findHistory(1).size()==5 && service.findClassRecords(1,2026,AcademicPeriod.FIRST).size()==2,"Academic history and class records");
            rejected(() -> service.saveRecord(record(2,1,AcademicPeriod.FINAL,"4.5")));
            LocalDate start=LocalDate.of(2026,10,19);
            Attendance day=attendance(1,start,AttendanceStatus.ABSENT);int attendanceId=service.saveAttendance(day,AcademicPeriod.FOURTH);
            check(service.getAttendance(attendanceId).getStatus()==AttendanceStatus.ABSENT,"Attendance mapping");
            rejected(() -> service.saveAttendance(attendance(1,start,AttendanceStatus.PRESENT),AcademicPeriod.FOURTH));
            day.setStatus(AttendanceStatus.PRESENT);service.saveAttendance(day,AcademicPeriod.FOURTH);
            check(service.getAttendance(attendanceId).getStatus()==AttendanceStatus.PRESENT,"Attendance correction");
            day.setStatus(AttendanceStatus.ABSENT);service.saveAttendance(day,AcademicPeriod.FOURTH);
            for(int i=1;i<4;i++) service.saveAttendance(attendance(1,start.plusDays(i),AttendanceStatus.ABSENT),AcademicPeriod.FOURTH);
            service.saveAttendance(attendance(2,start,AttendanceStatus.ABSENT),AcademicPeriod.FIRST);
            service.saveAttendance(attendance(1,start.plusDays(4),AttendanceStatus.MEDICAL_EXCUSE),AcademicPeriod.FOURTH);
            List<AbsenceAlert> alerts=service.weeklyAbsenceAlerts(start,3);
            check(alerts.size()==1 && alerts.getFirst().absenceDays()==4,"Count distinct days and exclude medical excuses");
            check(service.weeklyAbsenceAlerts(start,4).isEmpty(),"Threshold is strictly greater than");
            check(service.findAttendance(1,start,start.plusDays(6)).size()==6,"Date-filtered attendance");
            check(service.findClassAttendance(1,start).size()==1,"Class daily attendance");
            Attendance unauthorized=attendance(1,start.plusDays(5),AttendanceStatus.PRESENT);unauthorized.setTeacherId(2);
            rejected(() -> service.saveAttendance(unauthorized,AcademicPeriod.FOURTH));
            rejected(() -> service.findAttendance(1,start.plusDays(1),start));
            rejected(() -> service.saveAttendance(attendance(1,start,AttendanceStatus.PRESENT),AcademicPeriod.FINAL));
            int closed=service.enroll(enrollment(2,2,AcademicPeriod.SECOND));service.updateEnrollmentStatus(closed,EnrollmentStatus.WITHDRAWN);
            rejected(() -> service.updateEnrollmentStatus(closed,EnrollmentStatus.ACTIVE));
            rejected(() -> service.saveRecord(record(2,2,AcademicPeriod.SECOND,"4.0")));
            service.updateEnrollmentStatus(firstId,EnrollmentStatus.COMPLETED);
            check(service.getEnrollment(firstId).getStatus()==EnrollmentStatus.COMPLETED && service.findHistory(1).size()==5,"Closing enrollment preserves grades");
            rejected(() -> service.getRecord(999999));
        }
        System.out.println("Passed "+checks+" academic JDBC checks using temporary tables.");
    }
}
