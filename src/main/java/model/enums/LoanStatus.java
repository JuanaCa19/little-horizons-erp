package model.enums;

public enum LoanStatus {
    ACTIVE("Activo"),
    RETURNED("Devuelto"),
    OVERDUE("Vencido");
    private final String dbValue;
    LoanStatus(String value) { dbValue=value; }
    public String getDbValue() { return dbValue; }
    public static LoanStatus fromDbValue(String value) {
        for(LoanStatus item:values()) if(item.dbValue.equals(value)) return item;
        throw new IllegalArgumentException("Unknown LoanStatus: "+value);
    }
}
