package cz.meteocar.unit.engine.network.task;

/**
 * Object for transfer of queryParameters.
 */
public class QueryParameter {

    private String key;
    private String value;

    public QueryParameter(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
