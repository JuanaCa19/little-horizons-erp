package model.enums;

public enum EventType {
    ACADEMIC("Académico"),
    CULTURAL("Cultural"),
    SPORTS("Deportivo"),
    SOCIAL("Social"),
    SCIENTIFIC("Científico");
    private final String dbValue;
    EventType(String dbValue) { this.dbValue=dbValue; }
    public String getDbValue() { return dbValue; }
    public static EventType fromDbValue(String value) {
        for(EventType item:values()) if(item.dbValue.equals(value)) return item;
        throw new IllegalArgumentException("Unknown EventType: "+value);
    }
}
