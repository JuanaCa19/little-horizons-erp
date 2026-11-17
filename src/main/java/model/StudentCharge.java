package model;

import java.time.LocalDate;
import java.math.BigDecimal;
import model.enums.*;

public class StudentCharge {
    private Integer id;
    private Integer studentId;
    private Integer conceptId;
    private BigDecimal amount;
    private int academicYear;
    private PaymentPeriod period;
    private int month;
    private LocalDate dueDate;
    private ChargeStatus status;
    private Integer paymentId;

    public StudentCharge() { }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id=id; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId=studentId; }

    public Integer getConceptId() { return conceptId; }
    public void setConceptId(Integer conceptId) { this.conceptId=conceptId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount=amount; }

    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear=academicYear; }

    public PaymentPeriod getPeriod() { return period; }
    public void setPeriod(PaymentPeriod period) { this.period=period; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month=month; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate=dueDate; }

    public ChargeStatus getStatus() { return status; }
    public void setStatus(ChargeStatus status) { this.status=status; }

    public Integer getPaymentId() { return paymentId; }
    public void setPaymentId(Integer paymentId) { this.paymentId=paymentId; }
}
