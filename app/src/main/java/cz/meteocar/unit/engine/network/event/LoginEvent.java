package cz.meteocar.unit.engine.network.event;

import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.event.EventType;
import cz.meteocar.unit.engine.network.dto.LoginResponse;

/**
 * Login event.
 */
public class LoginEvent extends AppEvent {

    private LoginResponse response;

    public LoginEvent(LoginResponse response) {
        this.response = response;
    }

    public LoginResponse getResponse() {
        return response;
    }

    @Override
    public EventType getType() {
        return EventType.EVENT_NETWORK;
    }
}
