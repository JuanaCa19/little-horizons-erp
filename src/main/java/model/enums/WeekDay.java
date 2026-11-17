package model.enums;

public enum WeekDay {
    MONDAY("Lunes"),
    TUESDAY("Martes"),
    WEDNESDAY("Miércoles"),
    THURSDAY("Jueves"),
    FRIDAY("Viernes"),
    SATURDAY("Sábado");
    private final String dbValue;
    WeekDay(String dbValue) { this.dbValue = dbValue; }
    public String getDbValue() { return dbValue; }
    public static WeekDay fromDbValue(String value) {
        for (WeekDay item : values()) if (item.dbValue.equals(value)) return item;
        throw new IllegalArgumentException("Unknown WeekDay: " + value);
    }
}
