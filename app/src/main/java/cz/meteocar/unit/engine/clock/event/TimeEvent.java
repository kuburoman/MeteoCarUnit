package cz.meteocar.unit.engine.clock.event;

import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.event.EventType;

/**
 * Event for Time message.
 */
public class TimeEvent extends AppEvent {

    private String time;

    /**
     * @param myTime time
     */
    public TimeEvent(String myTime) {
        time = myTime;
    }

    /**
     * @return time
     */
    public String getTime() {
        return time;
    }

    @Override
    public EventType getType() {
        return EventType.EVENT_CLOCK;
    }

}
