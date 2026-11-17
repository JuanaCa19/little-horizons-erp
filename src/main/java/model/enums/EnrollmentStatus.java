package model.enums;

public enum EnrollmentStatus {
    ACTIVE("Activa"),
    COMPLETED("Completada"),
    WITHDRAWN("Retirada");

    private final String dbValue;

    EnrollmentStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static EnrollmentStatus fromDbValue(String dbValue) {
        for (EnrollmentStatus status : values()) {
            if (status.dbValue.equals(dbValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown enrollment status: " + dbValue);
    }
}
