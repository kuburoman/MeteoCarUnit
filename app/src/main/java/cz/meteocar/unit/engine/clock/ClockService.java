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
 * Created by Toms, 2014
 */
public class ClockService extends Thread {

    private boolean threadRun = false;
    private Thread loopThread;
    private SimpleDateFormat dateFormat;

    public MBassador<AppEvent> eventBus;

    /**
     * Inicializuje službu
     */
    public ClockService() {

        // data format
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // event bus
        eventBus = ServiceManager.getInstance().eventBus;

        // start - základ. služba, tu budeme chtít vždy
        start();
    }

    /**
     * Spustí službu
     */
    @Override
    public synchronized void start() {
        threadRun = true;
        super.start();
    }

    /**
     * Je služba spuštěna?
     *
     * @return True pokud ano, False pokud ne
     */
    public boolean isRunning() {
        return threadRun;
    }

    /**
     * Nastaví přízna ukončení služby
     */
    public void exit() {
        threadRun = false;
    }

    /**
     * Vytvoří časovou událost a odešle ji na bus
     */
    private void fireUpdateEvent() {

        // datum
        Date now = new Date();
        String strDate = dateFormat.format(now);

        // event fire
        //AppLog.i(null, "Time changed to: " + strDate);
        eventBus.post(new TimeEvent(strDate)).asynchronously();
    }

    /**
     * Hlavní cyklus služby
     */
    @Override
    public void run() {
        try {
            while (threadRun) {
                this.sleep(1000);
                fireUpdateEvent();
            }
        } catch (InterruptedException e) {
            Log.e(AppLog.LOG_TAG_DEFAULT, "ClockService.sleep() caused error.", e);
        }
    }

}
