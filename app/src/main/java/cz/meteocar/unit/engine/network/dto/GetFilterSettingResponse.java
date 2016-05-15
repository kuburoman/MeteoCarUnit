package cz.meteocar.unit.engine.network.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Get filter setting response.
 */
public class GetFilterSettingResponse {

    private List<FilterSettingDto> records;

    public List<FilterSettingDto> getRecords() {
        if (records == null) {
            records = new ArrayList<>();
        }
        return records;
    }
}
