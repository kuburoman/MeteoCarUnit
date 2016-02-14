package cz.meteocar.unit.engine.convertor;

import android.util.Log;

import org.json.JSONObject;

import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.helper.filter.RecordVO;
import cz.meteocar.unit.engine.storage.model.RecordEntity;

/**
 * Created by Nell on 24.1.2016.
 */
public class RecordVO2EntityConverter extends AbstractConverter<RecordVO, RecordEntity> {

    @Override
    protected RecordEntity innerConvert(RecordVO input) {
        RecordEntity res = new RecordEntity();

        res.setTime(input.getTime());
        res.setUserName(input.getUserId());
        res.setTripId(input.getTripId());
        res.setType(input.getType());

        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("value", input.getValue());
        } catch (Exception e) {
            Log.e(AppLog.LOG_TAG_DB, "Exception while adding OBD event data to JSON object", e);
        }

        res.setJson(jsonObj.toString());

        return res;
    }
}
