package cz.meteocar.unit.engine.network.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nell on 14.3.2016.
 */
public class CreateFilterSettingRequest {

    private List<FilterSettingDto> records;

    public CreateFilterSettingRequest() {
    }

    public CreateFilterSettingRequest(List<FilterSettingDto> records) {
        this.records = records;
    }

    public List<FilterSettingDto> getRecords() {
        if (records == null) {
            records = new ArrayList<>();
        }
        return records;
    }
}
