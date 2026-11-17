package model;

public class StudentGuardian {
    private Integer guardianId;
    private Integer studentId;
    private String relationShip;
    private boolean primary;
    private boolean financialResponsible;
    private boolean emergencyContact;

    public StudentGuardian() {
    }

    public StudentGuardian(Integer guardianId, Integer studentId, String relationShip, boolean primary, boolean financialResponsible, boolean emergencyContact) {
        this.guardianId = guardianId;
        this.studentId = studentId;
        this.relationShip = relationShip;
        this.primary = primary;
        this.financialResponsible = financialResponsible;
        this.emergencyContact = emergencyContact;
    }

    public Integer getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(Integer guardianId) {
        this.guardianId = guardianId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getRelationShip() {
        return relationShip;
    }

    public void setRelationShip(String relationShip) {
        this.relationShip = relationShip;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public boolean isFinancialResponsible() {
        return financialResponsible;
    }

    public void setFinancialResponsible(boolean financialResponsible) {
        this.financialResponsible = financialResponsible;
    }

    public boolean isEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(boolean emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    @Override
    public String toString() {
        return "StudentGuardian{" +
                "guardianId=" + guardianId +
                ", studentId=" + studentId +
                ", relationShip='" + relationShip + '\'' +
                ", primary=" + primary +
                ", financialResponsible=" + financialResponsible +
                ", emergencyContact=" + emergencyContact +
                '}';
    }
}
