package cz.meteocar.unit.engine.network.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Create OBD Pid request transport object.
 */
public class CreateOBDPidRequest {

    private List<OBDPidDto> records;

    public CreateOBDPidRequest() {
    }

    public CreateOBDPidRequest(List<OBDPidDto> records) {
        this.records = records;
    }

    public List<OBDPidDto> getRecords() {
        if (records == null) {
            records = new ArrayList<>();
        }
        return records;
    }

    public void setRecords(List<OBDPidDto> records) {
        this.records = records;
    }
}
