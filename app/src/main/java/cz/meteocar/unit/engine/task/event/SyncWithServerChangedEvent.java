package cz.meteocar.unit.engine.task.event;

import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.event.EventType;

/**
 * Event that inform TaskManager that synchronisation changed.
 */
public class SyncWithServerChangedEvent extends AppEvent {

    @Override
    public EventType getType() {
        return EventType.EVENT_SYNC_SWITCH;
    }
}
