package cz.meteocar.unit.engine.task;

import android.util.Log;

import java.util.TimerTask;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.event.ErrorViewType;
import cz.meteocar.unit.engine.event.NetworkErrorEvent;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.network.NetworkException;

/**
 * This is abstract task with common methods for Timer Tasks.
 */
public abstract class AbstractTask extends TimerTask {

    public abstract void runTask();

    @Override
    public void run() {
        Log.d(AppLog.LOG_TAG_DEFAULT, this.getClass().getName());
        try {
            runTask();
        } catch (Exception e) {
            Log.e(AppLog.LOG_TAG_DEFAULT, e.getMessage(), e);
        }
    }

    protected boolean isNetworkReady() {
        return ServiceManager.getInstance().getNetwork().isOnline();
    }

    protected void postNetworkException(NetworkException e) {
        ServiceManager.getInstance().eventBus.post(new NetworkErrorEvent(e.getErrorResponse(), ErrorViewType.DASHBOARD)).asynchronously();
    }

    protected void logException(NetworkException exception) {
        Log.e(AppLog.LOG_TAG_NETWORK, exception.getMessage(), exception);
        postNetworkException(exception);
    }

}
