package model;

import java.time.LocalDate;
import model.enums.*;

public class ResourceLoan {
    private Integer id;
    private Integer resourceId;
    private BorrowerType borrowerType;
    private Integer borrowerId;
    private LocalDate loanDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private LoanStatus status;
    private String notes;

    public ResourceLoan() { }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id=id; }

    public Integer getResourceId() { return resourceId; }
    public void setResourceId(Integer resourceId) { this.resourceId=resourceId; }

    public BorrowerType getBorrowerType() { return borrowerType; }
    public void setBorrowerType(BorrowerType borrowerType) { this.borrowerType=borrowerType; }

    public Integer getBorrowerId() { return borrowerId; }
    public void setBorrowerId(Integer borrowerId) { this.borrowerId=borrowerId; }

    public LocalDate getLoanDate() { return loanDate; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate=loanDate; }

    public LocalDate getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(LocalDate expectedReturnDate) { this.expectedReturnDate=expectedReturnDate; }

    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDate actualReturnDate) { this.actualReturnDate=actualReturnDate; }

    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status=status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes=notes; }

    public LoanStatus effectiveStatus(LocalDate referenceDate) {
        if(status==LoanStatus.RETURNED) return LoanStatus.RETURNED;
        return expectedReturnDate.isBefore(referenceDate) ? LoanStatus.OVERDUE : LoanStatus.ACTIVE;
    }

}
