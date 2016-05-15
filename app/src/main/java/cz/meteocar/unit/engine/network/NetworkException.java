package cz.meteocar.unit.engine.network;

import cz.meteocar.unit.engine.network.dto.ErrorResponse;

/**
 * Network exception.
 */
public class NetworkException extends Exception {

    private final ErrorResponse errorResponse;

    public NetworkException(ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
