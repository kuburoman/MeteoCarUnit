package cz.meteocar.unit.engine.storage.model;

/**
 * Created by Nell on 12.12.2015.
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
