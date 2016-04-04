package cz.meteocar.unit.engine.event;

/**
 * Created by Nell on 4.4.2016.
 */
public class DebugMessageEvent extends AppEvent {

    private String message;

    public DebugMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public EventType getType() {
        return EventType.EVENT_DEBUG_MESSAGE;
    }
}
