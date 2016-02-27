package cz.meteocar.unit.engine.storage.event;

import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.event.EventType;

/**
 * Created by Nell on 27.2.2016.
 */
public class DBEvent extends AppEvent {

    private long count;
    private int time;
    private double gpsDistance;
    private double obdDistance;

    public DBEvent(long count, int time, double obdDistance, double gpsDistance) {
        this.count = count;
        this.time = time;
        this.obdDistance = obdDistance;
        this.gpsDistance = gpsDistance;
    }

    public long getCount() {
        return count;
    }

    public int getTime() {
        return time;
    }

    public double getGpsDistance() {
        return gpsDistance;
    }

    public double getObdDistance() {
        return obdDistance;
    }

    @Override
    public EventType getType() {
        return EventType.EVENT_DB;
    }

}
