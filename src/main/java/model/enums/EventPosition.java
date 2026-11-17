package model.enums;

public enum EventPosition {
    FIRST("1er Lugar"),
    SECOND("2do Lugar"),
    THIRD("3er Lugar"),
    PARTICIPANT("Participante");
    private final String dbValue;
    EventPosition(String dbValue) { this.dbValue=dbValue; }
    public String getDbValue() { return dbValue; }
    public static EventPosition fromDbValue(String value) {
        for(EventPosition item:values()) if(item.dbValue.equals(value)) return item;
        throw new IllegalArgumentException("Unknown EventPosition: "+value);
    }
}
