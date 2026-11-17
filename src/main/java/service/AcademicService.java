package service;

import dao.*;
import dao.impl.*;
import java.math.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import model.*;
import model.enums.*;
import util.ConnectionDB;
import util.exception.ValidationException;
import static service.ServiceValidation.*;

public class AcademicService {
    private final SubjectDAO subjects;
    private final SubjectEnrollmentDAO enrollments;
    private final AcademicRecordDAO records;
    private final AttendanceDAO attendance;
    private final AcademicContextDAO context;
    private final TransactionManager transactions;
    public AcademicService() {
        this(new SubjectDAOImpl(),new SubjectEnrollmentDAOImpl(),new AcademicRecordDAOImpl(),new AttendanceDAOImpl(),
                new AcademicContextDAOImpl(),ConnectionDB::getConnection);
    }
    public AcademicService(SubjectDAO subjects,SubjectEnrollmentDAO enrollments,AcademicRecordDAO records,
                           AttendanceDAO attendance,AcademicContextDAO context,Supplier<Connection> connections) {
        this.subjects=Objects.requireNonNull(subjects);this.enrollments=Objects.requireNonNull(enrollments);
        this.records=Objects.requireNonNull(records);this.attendance=Objects.requireNonNull(attendance);
        this.context=Objects.requireNonNull(context);this.transactions=new TransactionManager(connections);
    }
    private void period(AcademicPeriod period,boolean allowFinal) {
        require(period!=null && (allowFinal || period!=AcademicPeriod.FINAL),"Seleccione un periodo entre I y IV.");
    }
    private Subject subject(Integer subjectId) {
        id(subjectId);return subjects.findById(subjectId).orElseThrow(() -> new ValidationException("Materia no encontrada."));
    }
    private Enrollment requireEnrollment(Integer studentId,int year,Subject subject,Connection c) {
        Enrollment enrollment=context.findEnrollment(studentId,year,c).orElseThrow(() -> new ValidationException("El estudiante no tiene matricula para ese anio."));
        require(Objects.equals(enrollment.getGradeId(),subject.getGradeId()),"La materia no corresponde al grado matriculado.");
        require(enrollment.getStatus()!=EnrollmentStatus.WITHDRAWN,"La matricula esta retirada.");
        return enrollment;
    }
    private SubjectEnrollment registration(Integer studentId,Integer subjectId,int year,AcademicPeriod period,Connection c) {
        SubjectEnrollment registration=enrollments.findByKey(studentId,subjectId,year,period,c)
                .orElseThrow(() -> new ValidationException("El estudiante no esta inscrito en la materia y periodo."));
        require(registration.getStatus()!=EnrollmentStatus.WITHDRAWN,"La inscripcion esta retirada.");return registration;
    }
    private void validateDate(LocalDate date,int year) {
        require(date!=null && date.getYear()==year && !date.isAfter(LocalDate.now()),"La fecha debe pertenecer al anio academico y no ser futura.");
    }
    public int enroll(SubjectEnrollment value) {
        require(value!=null,"La inscripcion es obligatoria.");require(value.getId()==null,"La nueva inscripcion no debe tener identificador.");
        id(value.getStudentId());year(value.getAcademicYear());period(value.getPeriod(),false);
        String section=text(value.getSection(),"La seccion").toUpperCase(Locale.ROOT);
        require(section.length()<=10,"La seccion admite hasta 10 caracteres.");
        require(value.getEnrollmentDate()!=null && value.getEnrollmentDate().getYear()==value.getAcademicYear(),"La fecha de inscripcion debe pertenecer al anio academico.");
        require(value.getStatus()==null || value.getStatus()==EnrollmentStatus.ACTIVE,"La nueva inscripcion debe estar activa.");
        Subject subject=subject(value.getSubjectId());require(subject.isActive(),"La materia esta inactiva.");
        int id=transactions.execute(c -> {
            context.lockStudent(value.getStudentId(),c);
            require(context.isStudentActive(value.getStudentId(),c),"Solo se pueden inscribir estudiantes activos.");
            Enrollment enrollment=requireEnrollment(value.getStudentId(),value.getAcademicYear(),subject,c);
            require(enrollment.getStatus()==EnrollmentStatus.ACTIVE,"La matricula debe estar activa.");
            require(!value.getEnrollmentDate().isBefore(enrollment.getEnrollmentDate()),"La inscripcion no puede ser anterior a la matricula.");
            require(enrollments.findByKey(value.getStudentId(),value.getSubjectId(),value.getAcademicYear(),value.getPeriod(),c).isEmpty(),"Ya existe una inscripcion para esa materia y periodo.");
            SubjectEnrollment pending=new SubjectEnrollment();
            pending.setStudentId(value.getStudentId());pending.setSubjectId(value.getSubjectId());pending.setAcademicYear(value.getAcademicYear());
            pending.setPeriod(value.getPeriod());pending.setSection(section);pending.setEnrollmentDate(value.getEnrollmentDate());pending.setStatus(EnrollmentStatus.ACTIVE);
            return enrollments.create(pending,c);
        });
        value.setId(id);value.setSection(section);value.setStatus(EnrollmentStatus.ACTIVE);return id;
    }
    public SubjectEnrollment getEnrollment(Integer enrollmentId) {
        id(enrollmentId);return transactions.execute(c -> enrollments.findById(enrollmentId,c).orElseThrow(() -> new ValidationException("Inscripcion no encontrada.")));
    }
    public void updateEnrollmentStatus(Integer enrollmentId,EnrollmentStatus status) {
        SubjectEnrollment snapshot=getEnrollment(enrollmentId);require(status!=null,"El estado es obligatorio.");
        transactions.execute(c -> {
            context.lockStudent(snapshot.getStudentId(),c);
            SubjectEnrollment existing=enrollments.findById(enrollmentId,c).orElseThrow(() -> new ValidationException("Inscripcion no encontrada."));
            if(existing.getStatus()==status) return null;
            require(existing.getStatus()==EnrollmentStatus.ACTIVE,"Una inscripcion cerrada no puede cambiar de estado.");
            enrollments.updateStatus(enrollmentId,status,c);return null;
        });
    }
    public List<SubjectEnrollment> findEnrollments(Integer studentId,int year) {
        id(studentId);ServiceValidation.year(year);return enrollments.findByStudentAndYear(studentId,year);
    }
    public List<SubjectEnrollment> findClassEnrollments(Integer subjectId,int year,AcademicPeriod period) {
        subject(subjectId);ServiceValidation.year(year);period(period,false);return enrollments.findBySubjectAndPeriod(subjectId,year,period);
    }
    public int saveRecord(AcademicRecord value) {
        require(value!=null,"La calificacion es obligatoria.");id(value.getStudentId());id(value.getTeacherId());year(value.getAcademicYear());period(value.getPeriod(),true);
        require(value.getGrade()!=null && value.getGrade().compareTo(BigDecimal.ZERO)>=0 && value.getGrade().compareTo(new BigDecimal("5.0"))<=0
                && value.getGrade().stripTrailingZeros().scale()<=1,"La nota debe estar entre 0.0 y 5.0 con maximo un decimal.");
        validateDate(value.getRecordDate(),value.getAcademicYear());Subject subject=subject(value.getSubjectId());
        int id=transactions.execute(c -> {
            context.lockStudent(value.getStudentId(),c);Enrollment enrollment=requireEnrollment(value.getStudentId(),value.getAcademicYear(),subject,c);
            require(!value.getRecordDate().isBefore(enrollment.getEnrollmentDate()),"La nota no puede ser anterior a la matricula.");
            SubjectEnrollment registration=null;
            for(AcademicPeriod period : AcademicPeriod.values()) {
                if(period==AcademicPeriod.FINAL) continue;
                if(value.getPeriod()==AcademicPeriod.FINAL || value.getPeriod()==period) {
                    registration=registration(value.getStudentId(),value.getSubjectId(),value.getAcademicYear(),period,c);
                    require(!value.getRecordDate().isBefore(registration.getEnrollmentDate()),"La nota no puede ser anterior a la inscripcion.");
                }
            }
            require(context.isTeacherAssigned(value.getTeacherId(),value.getSubjectId(),value.getAcademicYear(),registration.getSection(),c),
                    "El profesor no tiene asignada esa materia, anio y seccion o no esta activo.");
            Optional<AcademicRecord> existing=records.findByKey(value.getStudentId(),value.getSubjectId(),value.getAcademicYear(),value.getPeriod(),c);
            if(value.getId()==null) {
                require(existing.isEmpty(),"Ya existe una nota; use la opcion de corregir.");return records.create(value,c);
            }
            require(existing.isPresent() && Objects.equals(existing.get().getId(),value.getId()),"No se puede cambiar estudiante, materia, anio o periodo de una nota.");
            records.update(value,c);return value.getId();
        });value.setId(id);return id;
    }
    public AcademicRecord getRecord(Integer recordId) {
        id(recordId);return transactions.execute(c -> records.findById(recordId,c).orElseThrow(() -> new ValidationException("Nota no encontrada.")));
    }
    public List<AcademicRecord> findHistory(Integer studentId) { id(studentId);return records.findByStudentId(studentId); }
    public List<AcademicRecord> findClassRecords(Integer subjectId,int year,AcademicPeriod period) {
        subject(subjectId);ServiceValidation.year(year);period(period,true);return records.findBySubjectAndPeriod(subjectId,year,period);
    }
    private Optional<BigDecimal> average(List<AcademicRecord> values) {
        if(values.isEmpty()) return Optional.empty();
        BigDecimal sum=values.stream().map(AcademicRecord::getGrade).reduce(BigDecimal.ZERO,BigDecimal::add);
        return Optional.of(sum.divide(BigDecimal.valueOf(values.size()),2,RoundingMode.HALF_UP));
    }
    public Optional<BigDecimal> subjectAverage(Integer studentId,Integer subjectId,int year) {
        subject(subjectId);ServiceValidation.year(year);
        List<AcademicRecord> values=findHistory(studentId).stream().filter(value -> value.getSubjectId().equals(subjectId) && value.getAcademicYear()==year).toList();
        return values.stream().filter(value -> value.getPeriod()==AcademicPeriod.FINAL).findFirst()
                .map(value -> value.getGrade().setScale(2,RoundingMode.HALF_UP)).or(() -> average(values));
    }
    public Optional<BigDecimal> studentPeriodAverage(Integer studentId,int year,AcademicPeriod period) {
        ServiceValidation.year(year);period(period,true);
        return average(findHistory(studentId).stream().filter(value -> value.getAcademicYear()==year && value.getPeriod()==period).toList());
    }
    public Optional<BigDecimal> classAverage(Integer subjectId,int year,AcademicPeriod period,String section) {
        List<AcademicRecord> values=findClassRecords(subjectId,year,period);
        if(section==null || section.isBlank()) return average(values);
        String group=section.trim();AcademicPeriod enrollmentPeriod=period==AcademicPeriod.FINAL ? AcademicPeriod.FOURTH : period;
        return transactions.execute(c -> average(values.stream().filter(value -> enrollments.findByKey(value.getStudentId(),subjectId,year,enrollmentPeriod,c)
                .map(enrollment -> enrollment.getSection().equalsIgnoreCase(group)).orElse(false)).toList()));
    }
    public int saveAttendance(Attendance value,AcademicPeriod period) {
        require(value!=null,"La asistencia es obligatoria.");id(value.getStudentId());id(value.getTeacherId());period(period,false);
        require(value.getDate()!=null,"La fecha es obligatoria.");validateDate(value.getDate(),value.getDate().getYear());
        require(value.getStatus()!=null,"El estado de asistencia es obligatorio.");Subject subject=subject(value.getSubjectId());
        int id=transactions.execute(c -> {
            context.lockStudent(value.getStudentId(),c);Enrollment enrollment=requireEnrollment(value.getStudentId(),value.getDate().getYear(),subject,c);
            SubjectEnrollment registration=registration(value.getStudentId(),value.getSubjectId(),value.getDate().getYear(),period,c);
            require(!value.getDate().isBefore(registration.getEnrollmentDate()) && !value.getDate().isBefore(enrollment.getEnrollmentDate()),"La asistencia no puede ser anterior a la matricula o inscripcion.");
            require(context.isTeacherAssigned(value.getTeacherId(),value.getSubjectId(),value.getDate().getYear(),registration.getSection(),c),"El profesor no tiene asignada esa materia, anio y seccion o no esta activo.");
            Optional<Attendance> existing=attendance.findByKey(value.getStudentId(),value.getSubjectId(),value.getDate(),c);
            if(value.getId()==null) { require(existing.isEmpty(),"Ya existe asistencia para esa materia y fecha; use corregir.");return attendance.create(value,c); }
            require(existing.isPresent() && Objects.equals(existing.get().getId(),value.getId()),"No se puede cambiar estudiante, materia o fecha de una asistencia.");
            attendance.update(value,c);return value.getId();
        });value.setId(id);return id;
    }
    public Attendance getAttendance(Integer attendanceId) {
        id(attendanceId);return transactions.execute(c -> attendance.findById(attendanceId,c).orElseThrow(() -> new ValidationException("Asistencia no encontrada.")));
    }
    public List<Attendance> findAttendance(Integer studentId,LocalDate from,LocalDate to) {
        id(studentId);dateRange(from,to);return attendance.findByStudentAndDates(studentId,from,to);
    }
    private void dateRange(LocalDate from,LocalDate to) { require(from!=null && to!=null && !to.isBefore(from),"El rango de fechas no es valido."); }
    public List<Attendance> findClassAttendance(Integer subjectId,LocalDate date) {
        subject(subjectId);require(date!=null,"La fecha es obligatoria.");return attendance.findBySubjectAndDate(subjectId,date);
    }
    public List<AbsenceAlert> weeklyAbsenceAlerts(LocalDate weekStart,int threshold) {
        require(weekStart!=null,"La fecha inicial es obligatoria.");require(threshold>=0 && threshold<=7,"El umbral debe estar entre 0 y 7 dias.");
        return attendance.findAbsenceAlerts(weekStart,weekStart.plusDays(6),threshold);
    }
}
