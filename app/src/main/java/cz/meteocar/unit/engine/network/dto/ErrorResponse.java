package cz.meteocar.unit.engine.network.dto;

/**
 * Created by Nell on 28.2.2016.
 */
public class ErrorResponse {

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
