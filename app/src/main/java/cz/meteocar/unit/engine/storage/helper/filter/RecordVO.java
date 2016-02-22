package cz.meteocar.unit.engine.storage.helper.filter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Value object of Record.
 * It used mainly for Filer.
 */
public class RecordVO {

    private String type;
    private String tripId;
    private String userId;
    private Long time;
    private double value;
    private boolean saved;
    private Long lastSaved;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public Long getLastSaved() {
        return lastSaved;
    }

    public void setLastSaved(Long lastSaved) {
        this.lastSaved = lastSaved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RecordVO recordVO = (RecordVO) o;

        return new EqualsBuilder()
                .append(value, recordVO.value)
                .append(saved, recordVO.saved)
                .append(type, recordVO.type)
                .append(tripId, recordVO.tripId)
                .append(userId, recordVO.userId)
                .append(time, recordVO.time)
                .append(lastSaved, recordVO.lastSaved)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(type)
                .append(tripId)
                .append(userId)
                .append(time)
                .append(value)
                .append(saved)
                .append(lastSaved)
                .toHashCode();
    }
}
