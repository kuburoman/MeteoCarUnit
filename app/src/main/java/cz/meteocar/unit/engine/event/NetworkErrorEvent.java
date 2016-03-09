package cz.meteocar.unit.engine.event;

import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.event.EventType;
import cz.meteocar.unit.engine.network.dto.ErrorResponse;

/**
 * Created by Nell on 7.3.2016.
 */
public class NetworkErrorEvent extends AppEvent {

    private ErrorResponse errorResponse;
    private ErrorViewType view;

    public NetworkErrorEvent(ErrorResponse errorResponse, ErrorViewType view) {
        this.errorResponse = errorResponse;
        this.view = view;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    public ErrorViewType getView() {
        return view;
    }

    @Override
    public EventType getType() {
        return EventType.EVENT_NETWORK;
    }
}
