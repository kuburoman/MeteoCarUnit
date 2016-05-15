package cz.meteocar.unit.engine.network.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Create CarSetting request data transfer object.
 */
public class CreateCarSettingRequest {

    private List<CarSettingDto> records;

    public CreateCarSettingRequest(List<CarSettingDto> records) {
        this.records = records;
    }

    public List<CarSettingDto> getRecords() {
        if (records == null) {
            records = new ArrayList<>();
        }
        return records;
    }


}
