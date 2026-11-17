package model.enums;

public enum EventRole {
    PARTICIPANT("Participante"),
    COMPETITOR("Competidor"),
    ORGANIZER("Organizador"),
    ATTENDEE("Asistente");
    private final String dbValue;
    EventRole(String dbValue) { this.dbValue=dbValue; }
    public String getDbValue() { return dbValue; }
    public static EventRole fromDbValue(String value) {
        for(EventRole item:values()) if(item.dbValue.equals(value)) return item;
        throw new IllegalArgumentException("Unknown EventRole: "+value);
    }
}
