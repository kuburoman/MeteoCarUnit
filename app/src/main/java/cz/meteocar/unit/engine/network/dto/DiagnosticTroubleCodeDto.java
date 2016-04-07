package cz.meteocar.unit.engine.network.dto;

/**
 * Diagnostic trouble code Data Transfer Object
 */
public class DiagnosticTroubleCodeDto {

    private String tripHashcode;

    private Long time;

    private String code;

    public String getTripHashcode() {
        return tripHashcode;
    }

    public void setTripHashcode(String tripHashcode) {
        this.tripHashcode = tripHashcode;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
