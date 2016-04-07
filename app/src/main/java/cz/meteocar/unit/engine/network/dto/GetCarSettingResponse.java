package cz.meteocar.unit.engine.network.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nell on 14.3.2016.
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
