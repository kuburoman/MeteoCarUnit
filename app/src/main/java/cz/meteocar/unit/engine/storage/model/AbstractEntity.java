package cz.meteocar.unit.engine.storage.model;

/**
 * Abstract entity.
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
