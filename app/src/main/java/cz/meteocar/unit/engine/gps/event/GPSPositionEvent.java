package cz.meteocar.unit.engine.gps.event;

import android.location.Location;

import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.event.EventType;

/**
 * Event that carries GPS location in time.
 */
public class GPSPositionEvent extends AppEvent {

    private Location location;

    /**
     * @param myLocation GPS location
     */
    public GPSPositionEvent(Location myLocation) {
        super();
        location = myLocation;
    }

    /**
     * @return GPS location
     */
    public Location getLocation() {
        return location;
    }

    @Override
    public EventType getType() {
        return EventType.EVENT_GPS_POSITION;
    }
}
