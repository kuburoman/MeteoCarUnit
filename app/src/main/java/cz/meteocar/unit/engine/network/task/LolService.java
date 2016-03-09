package cz.meteocar.unit.engine.network.task;

import java.util.TimerTask;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.event.ErrorViewType;
import cz.meteocar.unit.engine.event.NetworkErrorEvent;
import cz.meteocar.unit.engine.network.dto.ErrorResponse;

/**
 * Created by Nell on 7.3.2016.
 */
public class LolService extends TimerTask {

    @Override
    public void run() {
        ServiceManager.getInstance().eventBus.post(new NetworkErrorEvent(new ErrorResponse("lol", "lol"), ErrorViewType.DASHBOARD)).asynchronously();
    }
}
