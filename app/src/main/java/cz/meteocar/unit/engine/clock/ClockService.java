package cz.meteocar.unit.engine.clock;

import net.engio.mbassy.bus.MBassador;

import java.text.SimpleDateFormat;
import java.util.Date;

import cz.meteocar.unit.engine.ServiceManager;

/**
 * Created by Toms, 2014
 */
public class ClockService extends Thread {

    private boolean threadRun = false;
    private Thread loopThread;
    private SimpleDateFormat dateFormat;

    public MBassador<ServiceManager.AppEvent> eventBus;

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
                synchronized (this) {
                    this.wait(1000);
                }
                fireUpdateEvent();
            }
        } catch (InterruptedException e) {

        }
    }

    /**
     * Třída pro zprávy event busu
     */
    public static class TimeEvent extends ServiceManager.AppEvent {
        String time;

        public TimeEvent(String myTime) {
            time = myTime;
        }

        public String getTime() {
            return time;
        }

        @Override
        public int getType() {
            return ServiceManager.AppEvent.EVENT_CLOCK;
        }
    }
}
