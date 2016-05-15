package cz.meteocar.unit.engine.enums;

/**
 * Enum for filter tags that are not connected with OBD.
 */
public enum RecordTypeEnum {
    TYPE_GPS("gps123"),
    TYPE_ACCEL("acc123");

    private final String value;

    RecordTypeEnum(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
