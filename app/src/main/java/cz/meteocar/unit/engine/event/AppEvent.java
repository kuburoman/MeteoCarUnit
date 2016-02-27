package cz.meteocar.unit.engine.event;

/**
 * Abstract event for all events.
 */
public abstract class AppEvent {

    protected String userId;
    protected String tripId;
    protected Long timeCreated;

    /**
     * Initializes time when event was created.
     */
    public AppEvent() {
        timeCreated = System.currentTimeMillis();
    }

    /**
     * Time when event was created.
     *
     * @return time in milliseconds from 1970
     */
    public Long getTimeCreated() {
        return timeCreated;
    }

    /**
     * @return Typ eventu
     */
    abstract public EventType getType();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

}
