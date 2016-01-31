package cz.meteocar.unit.engine.storage.helper.filter;

/**
 * Created by Nell on 18.7.2015.
 */
public enum ReducerType {

    ABSOLUTE(0), PERCENTAGE(1);

    private final int id;

    ReducerType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ReducerType fromId(int id) {
        for (ReducerType type : ReducerType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }
}
