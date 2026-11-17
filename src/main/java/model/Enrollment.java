package model;

import model.enums.EnrollmentStatus;

import java.time.LocalDate;

public class Enrollment {
    private Integer id;
    private Integer studentId;
    private Integer gradeId;
    private int academicYear;
    private EnrollmentStatus status;
    private LocalDate enrollmentDate;
    private LocalDate withdrawalDate;
    private String notes;

    public Enrollment() {
    }

    public Enrollment(Integer id, Integer studentId, Integer gradeId, int academicYear, EnrollmentStatus status, LocalDate enrollmentDate, LocalDate withdrawalDate, String notes) {
        this.id = id;
        this.studentId = studentId;
        this.gradeId = gradeId;
        this.academicYear = academicYear;
        this.status = status;
        this.enrollmentDate = enrollmentDate;
        this.withdrawalDate = withdrawalDate;
        this.notes = notes;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getGradeId() {
        return gradeId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public LocalDate getWithdrawalDate() {
        return withdrawalDate;
    }

    public void setWithdrawalDate(LocalDate withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", gradeId=" + gradeId +
                ", academicYear=" + academicYear +
                ", status=" + status +
                ", enrollmentDate=" + enrollmentDate +
                ", withdrawalDate=" + withdrawalDate +
                ", notes='" + notes + '\'' +
                '}';
    }
}
