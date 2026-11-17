package model.enums;

public enum MembershipStatus {
    ACTIVE("Activa"),
    WITHDRAWN("Retirada");
    private final String dbValue;
    MembershipStatus(String dbValue) { this.dbValue=dbValue; }
    public String getDbValue() { return dbValue; }
    public static MembershipStatus fromDbValue(String value) {
        for(MembershipStatus item:values()) if(item.dbValue.equals(value)) return item;
        throw new IllegalArgumentException("Unknown MembershipStatus: "+value);
    }
}
