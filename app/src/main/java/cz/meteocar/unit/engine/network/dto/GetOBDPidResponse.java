package cz.meteocar.unit.engine.network.dto;

import java.util.ArrayList;
import java.util.List;


/**
 * Get OBD Pid response transfer object.
 */
public class GetOBDPidResponse {

    public GetOBDPidResponse() {
    }

    public GetOBDPidResponse(List<OBDPidDto> records) {
        if (records == null) {
            this.records = new ArrayList<>();
        }
        this.records = records;
    }

    private List<OBDPidDto> records;

    public List<OBDPidDto> getRecords() {
        return records;
    }

    public void setRecords(List<OBDPidDto> records) {
        this.records = records;
    }
}
