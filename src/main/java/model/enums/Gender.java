package model.enums;

public enum Gender {
    MALE("M"),
    FEMALE("F"),
    OTHER("Otro");

    private final String dbValue;

    Gender(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static Gender fromDbValue(String dbValue) {
        for (Gender gender : values()) {
            if (gender.dbValue.equals(dbValue)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Unknown gender: " + dbValue);
    }
}
