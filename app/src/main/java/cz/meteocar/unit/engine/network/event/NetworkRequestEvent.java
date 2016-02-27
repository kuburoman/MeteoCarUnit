package cz.meteocar.unit.engine.network.event;

import org.json.JSONObject;

import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.event.EventType;

/**
 * Event that carries network request in json format.
 */
public class NetworkRequestEvent extends AppEvent {

    private String id;
    private JSONObject response;

    /**
     * @param myid       id of request
     * @param myResponse response in json format
     */
    public NetworkRequestEvent(String myid, JSONObject myResponse) {
        id = myid;
        response = myResponse;
    }

    /**
     * @return id of request
     */
    public String getID() {
        return id;
    }

    /**
     * @return response in json format
     */
    public JSONObject getResponse() {
        return response;
    }

    @Override
    public EventType getType() {
        return EventType.EVENT_NETWORK;
    }
}
