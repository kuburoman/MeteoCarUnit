package cz.meteocar.unit.engine.storage.model;

import cz.meteocar.unit.engine.storage.helper.filter.ReducerType;

/**
 * Created by Nell on 25.1.2016.
 */
public class FilterSettingEntity extends AbstractEntity {

    private String obdCode;
    private boolean active;
    private ReducerType reduceType;
    private double reduceValue;
    private boolean rounding;
    private int roundingDecimal;
    private Long maxTime;
    private Long updateTime;

    public String getObdCode() {
        return obdCode;
    }

    public void setObdCode(String obdCode) {
        this.obdCode = obdCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ReducerType getReduceType() {
        return reduceType;
    }

    public void setReduceType(ReducerType reduceType) {
        this.reduceType = reduceType;
    }

    public double getReduceValue() {
        return reduceValue;
    }

    public void setReduceValue(double reduceValue) {
        this.reduceValue = reduceValue;
    }

    public boolean isRounding() {
        return rounding;
    }

    public void setRounding(boolean rounding) {
        this.rounding = rounding;
    }

    public int getRoundingDecimal() {
        return roundingDecimal;
    }

    public void setRoundingDecimal(int roundingDecimal) {
        this.roundingDecimal = roundingDecimal;
    }

    public Long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(Long maxTime) {
        this.maxTime = maxTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}
