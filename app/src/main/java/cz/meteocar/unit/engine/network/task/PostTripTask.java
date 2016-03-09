package cz.meteocar.unit.engine.network.task;

import java.util.TimerTask;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.event.ErrorViewType;
import cz.meteocar.unit.engine.event.NetworkErrorEvent;
import cz.meteocar.unit.engine.network.NetworkException;
import cz.meteocar.unit.engine.storage.helper.TripHelper;
import cz.meteocar.unit.engine.storage.model.TripEntity;

/**
 * Created by Nell on 7.3.2016.
 */
public class PostTripTask extends TimerTask {

    private TripHelper tripHelper = ServiceManager.getInstance().db.getTripHelper();
    private NetworkConnector<String, Void> networkConnector = new NetworkConnector<>(String.class, Void.class, "trip");

    @Override
    public void run() {
        while (tripHelper.getNumberOfRecord() > 0) {
            TripEntity oneTrip = tripHelper.getOneTrip();
            try {
                networkConnector.post(oneTrip.getJson());
                tripHelper.delete(oneTrip.getId());
            } catch (NetworkException e) {
                ServiceManager.getInstance().eventBus.post(new NetworkErrorEvent(e.getErrorResponse(), ErrorViewType.DASHBOARD)).asynchronously();
            }

        }
    }
}
