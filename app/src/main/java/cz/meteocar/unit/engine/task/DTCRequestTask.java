package cz.meteocar.unit.engine.task;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.obd.event.DTCRequestEvent;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.helper.DTCHelper;
import cz.meteocar.unit.engine.task.AbstractTask;

/**
 * Adds request on DTC into obd message queue.
 */
public class DTCRequestTask extends AbstractTask {

    private DTCHelper helper = ServiceManager.getInstance().getDB().getDTCHelper();

    @Override
    public void runTask() {
        ServiceManager.getInstance().eventBus.post(new DTCRequestEvent()).asynchronously();
        helper.delete(DB.getTripId());
    }

}
