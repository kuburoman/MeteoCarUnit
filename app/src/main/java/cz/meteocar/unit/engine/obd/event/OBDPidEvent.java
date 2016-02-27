package cz.meteocar.unit.engine.obd.event;

import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.event.EventType;
import cz.meteocar.unit.engine.obd.OBDMessage;

/**
 * Event that carries information about OBD Pid requests.
 */
public class OBDPidEvent extends AppEvent {
    private OBDMessage msg;
    private double value;
    private String rawResponse;

    /**
     * @param msg     {@link OBDMessage}.
     * @param val     value that was calculated from msg.
     * @param rawResp response in raw form.
     */
    public OBDPidEvent(OBDMessage msg, double val, String rawResp) {
        this.msg = msg;
        this.value = val;
        this.rawResponse = rawResp;
        rawResponse = rawResp;

    }

    /**
     * @param timeCreated when request was created.
     */
    public void setTimeCreated(Long timeCreated) {
        this.timeCreated = timeCreated;
    }

    /**
     * @return {@link OBDMessage}.
     */
    public OBDMessage getMessage() {
        return msg;
    }

    /**
     * @return value that was calculated from msg.
     */
    public double getValue() {
        return value;
    }

    /**
     * @return response in raw form.
     */
    public String getRawResponse() {
        return rawResponse;
    }

    @Override
    public EventType getType() {
        return EventType.EVENT_OBD_PID;
    }
}
