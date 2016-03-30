package cz.meteocar.unit.engine.convertor;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import cz.meteocar.unit.engine.enums.RecordTypeEnum;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.helper.JsonTags;
import cz.meteocar.unit.engine.storage.model.RecordEntity;
import cz.meteocar.unit.engine.storage.simplify.DataPoint;

/**
 * Converts from {@link RecordEntity} to {@link DataPoint}.
 */
public class RecordEntity2DataPointConverter extends AbstractConverter<RecordEntity, DataPoint> {

    private static final String ACCEL_ENUM = RecordTypeEnum.TYPE_ACCEL.getValue();
    private static final String GPS_ENUM = RecordTypeEnum.TYPE_ACCEL.getValue();

    @Override
    protected DataPoint innerConvert(RecordEntity input) {
        try {
            if (ACCEL_ENUM.equals(input.getType())) {
                return createAccelerationDataPoint(input);
            }
            if (GPS_ENUM.equals(input.getType())) {
                return createGpsDataPoint(input);
            }
            return createOtherDataPoint(input);
        } catch (JSONException e) {
            Log.e(AppLog.LOG_TAG_DEFAULT, "Cannot convert entity with type: " + input.getType() + " json: " + input.getJson(), e);
            return null;
        }
    }

    protected DataPoint createAccelerationDataPoint(RecordEntity input) {
        return null;
    }

    protected DataPoint createGpsDataPoint(RecordEntity input) throws JSONException {
        JSONObject json = new JSONObject(input.getJson());
        return new DataPoint(input.getId(),
                json.getDouble(JsonTags.GPS_LAT) * 1000000,
                json.getDouble(JsonTags.GPS_LNG) * 1000000,
                input);
    }

    protected DataPoint createOtherDataPoint(RecordEntity input) throws JSONException {
        JSONObject json = new JSONObject(input.getJson());
        return new DataPoint(input.getId(),
                input.getTime(),
                json.getDouble(JsonTags.OTHER_VALUE),
                input);
    }
}
