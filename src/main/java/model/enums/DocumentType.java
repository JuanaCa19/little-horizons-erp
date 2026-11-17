package model.enums;

public enum DocumentType {

    CIVIL_REGISTRY("RC"),
    IDENTITY_CARD("TI"),
    CITIZENSHIP_CARD("CC"),
    FOREIGNER_CARD("CE"),
    TEMPORARY_PERMIT("PPT"),
    PASSPORT("Pasaporte");

    private final String dbValue;

    DocumentType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static DocumentType fromDbValue(String dbValue) {
        for (DocumentType type : values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown document type: " + dbValue);
    }
}
