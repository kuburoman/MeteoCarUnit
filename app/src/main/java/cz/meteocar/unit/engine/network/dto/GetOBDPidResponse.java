package cz.meteocar.unit.engine.network.dto;

import java.util.ArrayList;
import java.util.List;


/**
 * Get OBD Pid response transfer object.
 */
public class GetOBDPidResponse {

    private List<OBDPidDto> records;

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
