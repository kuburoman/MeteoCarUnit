package cz.meteocar.unit.engine.task.event;

import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.event.EventType;

/**
 * Event that informs TaskManager that he must reschedule all tasks.
 */
public class RescheduleTasksEvent extends AppEvent {

    @Override
    public EventType getType() {
        return EventType.EVENT_RESCHEDULE_TASKS;
    }
}
