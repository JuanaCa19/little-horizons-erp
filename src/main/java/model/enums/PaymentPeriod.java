package model.enums;

public enum PaymentPeriod {
    FIRST("I"),
    SECOND("II"),
    THIRD("III"),
    FOURTH("IV"),
    ANNUAL("Anual");
    private final String dbValue;
    PaymentPeriod(String value) { dbValue=value; }
    public String getDbValue() { return dbValue; }
    public static PaymentPeriod fromDbValue(String value) {
        for(PaymentPeriod item:values()) if(item.dbValue.equals(value)) return item;
        throw new IllegalArgumentException("Unknown PaymentPeriod: "+value);
    }
}
