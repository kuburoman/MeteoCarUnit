package cz.meteocar.unit.engine.obd.event;

import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.event.EventType;

/**
 * Event that carries status of OBD device.
 */
public class OBDStatusEvent extends AppEvent {

    private int statusCode;
    private String statusText;

    /**
     * @param statCode status
     * @param statText status in user friendly form
     */
    public OBDStatusEvent(int statCode, String statText) {
        statusCode = statCode;
        statusText = statText;
    }

    /**
     * @return status
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @return status in user friendly form
     */
    public String getStatusText() {
        return statusText;
    }

    @Override
    public EventType getType() {
        return EventType.EVENT_OBD_STATUS;
    }

}
