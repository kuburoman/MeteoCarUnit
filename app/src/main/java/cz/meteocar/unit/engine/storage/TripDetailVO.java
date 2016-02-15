package cz.meteocar.unit.engine.storage;

import android.location.Location;

/**
 * Created by Nell on 3.2.2016.
 */
public class TripDetailVO {

    private String tripId;
    private Long startTime;
    private Long endTime;

    public TripDetailVO(String tripId, Long startTime, Long endTime) {
        this.tripId = tripId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
