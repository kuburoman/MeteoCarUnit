package cz.meteocar.unit.engine.storage.model;

/**
 * Trip entity.
 */
public class TripEntity extends AbstractEntity {
    private String json;

    public TripEntity() {
    }

    public TripEntity(int id, String json) {
        this.id = id;
        this.json = json;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
