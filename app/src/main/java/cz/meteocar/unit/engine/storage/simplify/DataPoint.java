package cz.meteocar.unit.engine.storage.simplify;

import cz.meteocar.unit.engine.storage.model.RecordEntity;

/**
 * DataPoint used in simplification of data.
 */
public class DataPoint {

    private int id;
    private double valueX;
    private double valueY;
    private RecordEntity recordEntity;

    public DataPoint(int id, double valueX, double valueY, RecordEntity recordEntity) {
        this.id = id;
        this.valueX = valueX;
        this.valueY = valueY;
        this.recordEntity = recordEntity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getValueX() {
        return valueX;
    }

    public void setValueX(double valueX) {
        this.valueX = Double.valueOf(valueX).longValue();
    }

    public double getValueY() {
        return valueY;
    }

    public void setValueY(double valueY) {
        this.valueY = valueY;
    }

    public RecordEntity getRecordEntity() {
        return recordEntity;
    }

    public void setRecordEntity(RecordEntity recordEntity) {
        this.recordEntity = recordEntity;
    }
}
