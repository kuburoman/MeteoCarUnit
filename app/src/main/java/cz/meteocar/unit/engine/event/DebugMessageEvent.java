package cz.meteocar.unit.engine.event;

/**
 * Event for debug message.
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
