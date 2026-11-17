package model.enums;

public enum AttendanceStatus {
    PRESENT("Presente"),
    ABSENT("Ausente"),
    LATE("Tardanza"),
    MEDICAL_EXCUSE("Excusa Médica");
    private final String dbValue;
    AttendanceStatus(String dbValue) { this.dbValue = dbValue; }
    public String getDbValue() { return dbValue; }
    public static AttendanceStatus fromDbValue(String value) {
        for (AttendanceStatus item : values()) if (item.dbValue.equals(value)) return item;
        throw new IllegalArgumentException("Unknown AttendanceStatus: " + value);
    }
}
