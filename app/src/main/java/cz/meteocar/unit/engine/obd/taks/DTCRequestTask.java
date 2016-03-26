package cz.meteocar.unit.engine.obd.taks;

import java.util.TimerTask;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.obd.event.DTCRequestEvent;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.helper.DTCHelper;

/**
 * Adds request on DTC into obd message queue.
 */
public class DTCRequestTask extends TimerTask {

    private DTCHelper helper = ServiceManager.getInstance().db.getDTCHelper();

    @Override
    public void run() {
        ServiceManager.getInstance().eventBus.post(new DTCRequestEvent()).asynchronously();
        helper.delete(DB.getTripId());
    }
}
