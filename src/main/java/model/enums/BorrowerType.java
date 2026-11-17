package model.enums;

public enum BorrowerType {
    STUDENT("Estudiante"),
    TEACHER("Profesor");
    private final String dbValue;
    BorrowerType(String value) { dbValue=value; }
    public String getDbValue() { return dbValue; }
    public static BorrowerType fromDbValue(String value) {
        for(BorrowerType item:values()) if(item.dbValue.equals(value)) return item;
        throw new IllegalArgumentException("Unknown BorrowerType: "+value);
    }
}
