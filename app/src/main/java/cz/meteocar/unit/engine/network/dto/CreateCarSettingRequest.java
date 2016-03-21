package cz.meteocar.unit.engine.network.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nell on 14.3.2016.
 */
public class CreateCarSettingRequest {

    private List<CarSettingDto> records;

    public CreateCarSettingRequest() {
    }

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
