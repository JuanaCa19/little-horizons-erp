package model;

import java.time.LocalDate;
import java.math.BigDecimal;
import model.enums.*;

public class StudentPrice {
    private Integer id;
    private Integer studentId;
    private Integer conceptId;
    private BigDecimal amount;
    private String reason;
    private LocalDate validFrom;
    private LocalDate validUntil;

    public StudentPrice() { }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id=id; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId=studentId; }

    public Integer getConceptId() { return conceptId; }
    public void setConceptId(Integer conceptId) { this.conceptId=conceptId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount=amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason=reason; }

    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom=validFrom; }

    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil=validUntil; }
}
