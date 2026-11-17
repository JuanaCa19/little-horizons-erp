package model.enums;

public enum AcademicPeriod {
    FIRST("I"),
    SECOND("II"),
    THIRD("III"),
    FOURTH("IV"),
    FINAL("Final");
    private final String dbValue;
    AcademicPeriod(String dbValue) { this.dbValue = dbValue; }
    public String getDbValue() { return dbValue; }
    public static AcademicPeriod fromDbValue(String value) {
        for (AcademicPeriod item : values()) if (item.dbValue.equals(value)) return item;
        throw new IllegalArgumentException("Unknown AcademicPeriod: " + value);
    }
}
