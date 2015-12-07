package cz.meteocar.unit.engine.enums;

/**
 * Created by Nell on 7.12.2015.
 */
public enum RecordTypeEnum {
    TYPE_GPS("gps"),
    TYPE_ACCEL("accel");

    private final String value;

    RecordTypeEnum(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
