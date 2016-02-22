package cz.meteocar.unit.engine.storage.model;

/**
 * Created by Nell on 17.2.2016.
 */
public abstract class AbstractEntity {

    protected int id = -1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
