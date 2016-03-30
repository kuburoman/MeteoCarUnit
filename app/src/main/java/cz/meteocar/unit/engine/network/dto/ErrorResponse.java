package cz.meteocar.unit.engine.network.dto;

import java.io.Serializable;

/**
 * Error response from server.
 */
public class ErrorResponse implements Serializable {

    private static final long serialVersionUID = 12145315135431L;

    private String code;
    private String message;

    public ErrorResponse() {
        // Used by Json.from in tests
    }

    public ErrorResponse(String code, String message) {
        this.message = message;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
