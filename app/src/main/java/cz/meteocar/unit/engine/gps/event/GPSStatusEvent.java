package cz.meteocar.unit.engine.gps.event;

import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.event.EventType;

/**
 * Event that carries status of GPS device.
 */
public class GPSStatusEvent extends AppEvent {
    private int status;

    /**
     * @param stat status of GPS.
     */
    public GPSStatusEvent(int stat) {
        super();
        status = stat;
    }

    /**
     * @return Status of GPS.
     */
    public int getStatus() {
        return status;
    }

    @Override
    public EventType getType() {
        return EventType.EVENT_GPS_STATUS;
    }
}
