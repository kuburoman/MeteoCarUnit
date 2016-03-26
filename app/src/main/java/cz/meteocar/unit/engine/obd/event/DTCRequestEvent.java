package cz.meteocar.unit.engine.obd.event;

import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.event.EventType;

/**
 * Created by Nell on 26.3.2016.
 */
public class DTCRequestEvent extends AppEvent {

    @Override
    public EventType getType() {
        return EventType.EVENT_DTC_REQUEST;
    }
}
