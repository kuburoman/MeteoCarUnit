package cz.meteocar.unit.engine.network.event;

import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.event.EventType;
import cz.meteocar.unit.engine.network.NetworkStatus;

/**
 * Event that carries status of network connection.
 */
public class NetworkStatusEvent extends AppEvent {

    private NetworkStatus connectionType;
    private boolean connected;

    /**
     * @param connType {@link NetworkStatus}.
     * @param isConn   if we are connected to network.
     */
    public NetworkStatusEvent(NetworkStatus connType, boolean isConn) {
        connectionType = connType;
        connected = isConn;
    }

    /**
     * @return {@link NetworkStatus}.
     */
    public NetworkStatus getConnectionType() {
        return connectionType;
    }

    /**
     * @return True if we are connected or false.
     */
    public boolean isConnected() {
        return connected;
    }

    @Override
    public EventType getType() {
        return EventType.EVENT_NETWORK;
    }

}
