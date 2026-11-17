package model.enums;

public enum PaymentMethod {
    CASH("Efectivo"),
    TRANSFER("Transferencia"),
    DEBIT_CARD("Tarjeta Débito"),
    CREDIT_CARD("Tarjeta Crédito"),
    CHECK("Cheque");
    private final String dbValue;
    PaymentMethod(String value) { dbValue=value; }
    public String getDbValue() { return dbValue; }
    public static PaymentMethod fromDbValue(String value) {
        for(PaymentMethod item:values()) if(item.dbValue.equals(value)) return item;
        throw new IllegalArgumentException("Unknown PaymentMethod: "+value);
    }
}
