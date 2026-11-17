package model.enums;

public enum TeacherStatus {
    ACTIVE("Activo"),
    INACTIVE("Inactivo"),
    ON_LEAVE("Licencia");
    private final String dbValue;
    TeacherStatus(String dbValue) { this.dbValue = dbValue; }
    public String getDbValue() { return dbValue; }
    public static TeacherStatus fromDbValue(String value) {
        for (TeacherStatus item : values()) if (item.dbValue.equals(value)) return item;
        throw new IllegalArgumentException("Unknown TeacherStatus: " + value);
    }
}
