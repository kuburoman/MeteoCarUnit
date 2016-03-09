package cz.meteocar.unit.engine.storage;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.helper.RecordHelper;
import cz.meteocar.unit.engine.storage.helper.TripHelper;
import cz.meteocar.unit.engine.storage.model.RecordEntity;
import cz.meteocar.unit.engine.storage.model.TripEntity;

/**
 * Created by Nell on 13.12.2015.
 */
public class ConvertService extends TimerTask {

    private RecordHelper recordHelper;
    private TripHelper tripHelper;

    public ConvertService() {
        this(ServiceManager.getInstance().db.getRecordHelper(), ServiceManager.getInstance().db.getTripHelper());
    }

    public ConvertService(RecordHelper recordHelper, TripHelper tripHelper) {
        this.recordHelper = recordHelper;
        this.tripHelper = tripHelper;
    }

    @Override
    public void run() {
        Log.e(AppLog.LOG_TAG_NETWORK, "WTF");
        if (recordHelper.getNumberOfRecord(false) > 0) {
            createJsonRecords();
        }
    }

    protected JSONObject createJsonTrip(List<RecordEntity> recordList) throws JSONException {

        JSONArray jsonArray = new JSONArray();

        String userName = recordList.get(0).getUserName();
        String tripId = recordList.get(0).getTripId();

        for (RecordEntity recordEntity : recordList) {
            JSONObject object = new JSONObject();

            JSONObject value = new JSONObject(recordEntity.getJson());
            object.put("json", value);
            object.put("code", recordEntity.getType());
            object.put("time", recordEntity.getTime());

            jsonArray.put(object);
        }


        JSONObject main = new JSONObject();
        main.put("trip", tripId);
        main.put("user", userName);
        main.put("records", jsonArray);

        return main;
    }

    protected void createJsonRecords() {
        List<String> userIds = recordHelper.getUserIdStored();
        for (String userId : userIds) {
            if (userId == null) {
                recordHelper.deleteUserNullRecords();
                continue;
            }
            List<RecordEntity> entityList = recordHelper.getByUserId(userId, 100, false);
            if (entityList.size() < 1) {
                break;
            }

            JSONObject jsonTrip = new JSONObject();
            try {
                jsonTrip = createJsonTrip(entityList);
            } catch (JSONException e) {
                Log.e(AppLog.LOG_TAG_DEFAULT, "Cannot convert trip", e);
                e.printStackTrace();
            }

            tripHelper.save(new TripEntity(-1, jsonTrip.toString()));

            List<Integer> integers = new ArrayList<>();
            for (RecordEntity recordEntity : entityList) {
                integers.add(recordEntity.getId());
            }

            recordHelper.updateProcessed(integers, true);
            Log.d(AppLog.LOG_TAG_DB, "Successful creation of trip");
        }
    }
}
