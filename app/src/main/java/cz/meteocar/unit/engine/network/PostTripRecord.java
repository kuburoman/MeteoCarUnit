package cz.meteocar.unit.engine.network;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores json with trip records in JSON format and also list of id of trip records.
 */
public class PostTripRecord {

    public PostTripRecord(List<Integer> tripRecordIds, JSONObject tripRecordsJson) {
        this.tripRecordIds = tripRecordIds;
        this.tripRecordsJson = tripRecordsJson;
    }

    private List<Integer> tripRecordIds;
    private JSONObject tripRecordsJson;

    public List<Integer> getTripRecordIds() {
        if(tripRecordIds == null){
            tripRecordIds = new ArrayList<>();
        }
        return tripRecordIds;
    }

    public JSONObject getTripRecordsJson() {
        return tripRecordsJson;
    }

    public void setTripRecordsJson(JSONObject tripRecordsJson) {
        this.tripRecordsJson = tripRecordsJson;
    }
}
