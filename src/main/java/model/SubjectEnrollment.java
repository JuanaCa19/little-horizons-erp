package model;

import java.time.LocalDate;
import java.math.BigDecimal;
import model.enums.*;

public class SubjectEnrollment {
    private Integer id;
    private Integer studentId;
    private Integer subjectId;
    private int academicYear;
    private AcademicPeriod period;
    private String section;
    private LocalDate enrollmentDate;
    private EnrollmentStatus status;

    public SubjectEnrollment() { }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }

    public AcademicPeriod getPeriod() { return period; }
    public void setPeriod(AcademicPeriod period) { this.period = period; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }
}
