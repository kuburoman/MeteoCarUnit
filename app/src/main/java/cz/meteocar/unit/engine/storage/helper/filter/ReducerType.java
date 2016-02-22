package cz.meteocar.unit.engine.storage.helper.filter;

/**
 * Enum type for reduce type.
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

    /**
     * Returns enum type based on id.
     *
     * @param id of enum
     * @return {@link ReducerType}
     */
    public static ReducerType fromId(int id) {
        for (ReducerType type : ReducerType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }
}
