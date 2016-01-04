package cz.meteocar.unit.engine.storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.model.RecordEntity;
import cz.meteocar.unit.engine.storage.model.TripEntity;

/**
 * Created by Nell on 13.12.2015.
 */
public class ConvertService extends Thread {

    private boolean threadRun;

    public ConvertService() {
        // start threadu
        threadRun = true;
        start();
    }

    /**
     * Ukončí thread bezpečně
     */
    public void exit() {
        threadRun = false;
    }

    public JSONObject createJsonTrip(List<RecordEntity> recordList) throws JSONException {

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
        main.put("boardUnitId", "android2");
        main.put("secretKey","Ninjahash");
        main.put("trip", tripId);
        main.put("user", userName);
        main.put("records", jsonArray);

        return main;
    }

    public void createJsonRecords() throws JSONException {
        List<String> userIds = DB.recordHelper.getUserIdStored();
        List<RecordEntity> recordEntityList = new ArrayList<>();
        for (String userId : userIds) {
            while (true) {
                List<RecordEntity> entityList = DB.recordHelper.getByUserId(userId, 100);
                if (entityList.size() < 1){
                    break;
                }
                JSONObject jsonTrip = createJsonTrip(entityList);

                DB.tripHelper.save(new TripEntity(-1, jsonTrip.toString()));

                List<Integer> integers = new ArrayList<>();
                for (RecordEntity recordEntity : entityList) {
                    integers.add(recordEntity.getId());
                }

                DB.recordHelper.deleteRecords(integers);
                AppLog.i(AppLog.LOG_TAG_DB, "Successful creation of trip");
            }

        }
    }

    /**
     * Hlavní cyklus vlákna
     */
    @Override
    public void run() {
        try {

            while (threadRun) {
                if (DB.recordHelper.getNumberOfRecord() > 0) {
                    createJsonRecords();
                } else {

                    try {
                        this.sleep(60000);
                    } catch (Exception e) {
                        // nevadí
                    }
                }

            }
            //
            AppLog.i(AppLog.LOG_TAG_DB, "Database Service exited LOOP");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
