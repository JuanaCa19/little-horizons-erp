package model.enums;

public enum ResourceStatus {
    AVAILABLE("Disponible"),
    LOANED("Prestado"),
    MAINTENANCE("En Mantenimiento"),
    RETIRED("Dado de Baja");
    private final String dbValue;
    ResourceStatus(String value) { dbValue=value; }
    public String getDbValue() { return dbValue; }
    public static ResourceStatus fromDbValue(String value) {
        for(ResourceStatus item:values()) if(item.dbValue.equals(value)) return item;
        throw new IllegalArgumentException("Unknown ResourceStatus: "+value);
    }
}
