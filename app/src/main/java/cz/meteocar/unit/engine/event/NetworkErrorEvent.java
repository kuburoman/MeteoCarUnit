package cz.meteocar.unit.engine.event;

import cz.meteocar.unit.engine.network.dto.ErrorResponse;

/**
 * Network event error type.
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
