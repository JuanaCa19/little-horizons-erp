package model.enums;

public enum ChargeStatus {
    PENDING("Pendiente"),
    PAID("Pagado"),
    CANCELLED("Anulado");
    private final String dbValue;
    ChargeStatus(String value) { dbValue=value; }
    public String getDbValue() { return dbValue; }
    public static ChargeStatus fromDbValue(String value) {
        for(ChargeStatus item:values()) if(item.dbValue.equals(value)) return item;
        throw new IllegalArgumentException("Unknown ChargeStatus: "+value);
    }
}
