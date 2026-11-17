package model;

import java.time.LocalDate;
import java.math.BigDecimal;
import model.enums.*;

public class AcademicRecord {
    private Integer id;
    private Integer studentId;
    private Integer subjectId;
    private Integer teacherId;
    private int academicYear;
    private AcademicPeriod period;
    private BigDecimal grade;
    private String notes;
    private LocalDate recordDate;

    public AcademicRecord() { }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }

    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }

    public AcademicPeriod getPeriod() { return period; }
    public void setPeriod(AcademicPeriod period) { this.period = period; }

    public BigDecimal getGrade() { return grade; }
    public void setGrade(BigDecimal grade) { this.grade = grade; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
}
