package cz.meteocar.unit.engine.network.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Get car setting response.
 */
public class GetCarSettingResponse {

    private List<CarSettingDto> records;

    public List<CarSettingDto> getRecords() {
        if (records == null) {
            records = new ArrayList<>();
        }
        return records;
    }
}
