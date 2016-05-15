package cz.meteocar.unit.engine.clock;

import android.util.Log;

import net.engio.mbassy.bus.MBassador;

import java.text.SimpleDateFormat;
import java.util.Date;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.clock.event.TimeEvent;
import cz.meteocar.unit.engine.log.AppLog;

/**
 * Clock service to count seconds.
 */
public class ClockService extends Thread {

    private boolean threadRun = false;
    private SimpleDateFormat dateFormat;

    private MBassador<AppEvent> eventBus;

    /**
     * Initialize service
     */
    public ClockService() {

        // data format
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // event bus
        eventBus = ServiceManager.getInstance().eventBus;

        start();
    }

    /**
     * Run service.
     */
    @Override
    public synchronized void start() {
        threadRun = true;
        super.start();
    }

    /**
     * Sets clock service to exit.
     */
    public void exit() {
        threadRun = false;
    }

    private void fireUpdateEvent() {

        Date now = new Date();
        String strDate = dateFormat.format(now);

        eventBus.post(new TimeEvent(strDate)).asynchronously();
    }

    @Override
    public void run() {
        try {
            while (threadRun) {
                Thread.sleep(1000);
                fireUpdateEvent();
            }
        } catch (InterruptedException e) {
            Log.e(AppLog.LOG_TAG_DEFAULT, "ClockService.sleep() caused error.", e);
        }
    }

}
