package model;

import java.time.LocalDate;
import java.math.BigDecimal;
import model.enums.*;

public class Payment {
    private Integer id;
    private Integer studentId;
    private Integer guardianId;
    private Integer conceptId;
    private BigDecimal amount;
    private LocalDate date;
    private PaymentMethod method;
    private String receiptNumber;
    private int academicYear;
    private PaymentPeriod period;
    private String notes;

    public Payment() { }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id=id; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId=studentId; }

    public Integer getGuardianId() { return guardianId; }
    public void setGuardianId(Integer guardianId) { this.guardianId=guardianId; }

    public Integer getConceptId() { return conceptId; }
    public void setConceptId(Integer conceptId) { this.conceptId=conceptId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount=amount; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date=date; }

    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method=method; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber=receiptNumber; }

    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear=academicYear; }

    public PaymentPeriod getPeriod() { return period; }
    public void setPeriod(PaymentPeriod period) { this.period=period; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes=notes; }
}
