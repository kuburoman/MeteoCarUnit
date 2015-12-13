package cz.meteocar.unit.engine.network.event;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.enums.NetworkRequestResultEnum;
import cz.meteocar.unit.engine.network.NetworkService;

/**
 * Created by Nell on 12.12.2015.
 */
public class PostTripRecordsResultEvent extends ServiceManager.AppEvent {

    private NetworkRequestResultEnum requestResult;
    private List<Integer> tripRecordIds;

    public PostTripRecordsResultEvent(NetworkRequestResultEnum requestResult, List<Integer> tripRecordIds) {
        this.requestResult = requestResult;
        this.tripRecordIds = tripRecordIds;
    }

    public NetworkRequestResultEnum getRequestResult() {
        return requestResult;
    }

    public void setRequestResult(NetworkRequestResultEnum requestResult) {
        this.requestResult = requestResult;
    }

    public List<Integer> getTripRecordIds() {
        if(tripRecordIds == null){
            tripRecordIds = new ArrayList<>();
        }
        return tripRecordIds;
    }

    @Override
    public int getType() {
        return 0;
    }
}
