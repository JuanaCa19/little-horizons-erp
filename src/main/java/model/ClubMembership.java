package model;

import java.time.LocalDate;
import model.enums.*;

public class ClubMembership {
    private Integer id;
    private Integer studentId;
    private Integer clubId;
    private LocalDate entryDate;
    private MembershipStatus status;
    private LocalDate withdrawalDate;
    private String withdrawalReason;

    public ClubMembership() { }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id=id; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId=studentId; }

    public Integer getClubId() { return clubId; }
    public void setClubId(Integer clubId) { this.clubId=clubId; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate=entryDate; }

    public MembershipStatus getStatus() { return status; }
    public void setStatus(MembershipStatus status) { this.status=status; }

    public LocalDate getWithdrawalDate() { return withdrawalDate; }
    public void setWithdrawalDate(LocalDate withdrawalDate) { this.withdrawalDate=withdrawalDate; }

    public String getWithdrawalReason() { return withdrawalReason; }
    public void setWithdrawalReason(String withdrawalReason) { this.withdrawalReason=withdrawalReason; }
}
