package cz.meteocar.unit.engine.network;

import cz.meteocar.unit.engine.network.dto.ErrorResponse;

/**
 * Created by Nell on 7.3.2016.
 */
public class NetworkException extends Exception {

    private ErrorResponse errorResponse;

    public NetworkException(ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
