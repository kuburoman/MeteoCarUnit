package cz.meteocar.unit.engine.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import net.engio.mbassy.listener.Handler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.accel.AccelService;
import cz.meteocar.unit.engine.clock.ClockService;
import cz.meteocar.unit.engine.gps.ServiceGPS;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.obd.OBDService;

/**
 * Created by Toms, 2014.
 */
public class DatabaseService extends Thread {

    /**
     * Fronta na eventy
     * - zápisy do DB mohou chvíli trvat, proto je žádoucí je spuštět až ve vlastním vlákně,
     * které bude číst eventy z této fronty
     */
    BlockingQueue<ServiceManager.AppEvent> queue;

    private boolean threadRun;
    private Context context;

    // helper
    public DatabaseHelper helper;

    public DatabaseService(Context ctx){
        context = ctx;

        // nová fronta na eventy
        queue = new ArrayBlockingQueue<ServiceManager.AppEvent>(20);

        // helper
        helper = DB.helper = new DatabaseHelper(ctx);

        // přihlášení k odběru dat ze service busu
        ServiceManager.getInstance().eventBus.subscribe(this);

        //
        /*if(false || (System.currentTimeMillis() < (Long.parseLong("1416869153244")+2*1000*60))){
            trip.deleteAllRecords();
        }
        if(true){
            trip.listAll();
        }*/
        //trip.saveToFileAndClear();

        // inicializujeme stav zaznamenávání jízdy
        initTripRecording();

        // start threadu
        threadRun = true;
        start();
    }

    // ---------- Záznam z jízdy -----------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    private int seconds; public int getTripTime(){ return seconds; }
    private long count;
    private double gpsDistance;
    private double obdDistance; public double getTripDistance(){ return obdDistance; }
    private long tripStart; public long getTripStart(){ return tripStart; }
    private long tripStop; public long getTripStop(){ return tripStop; }
    private Location gpsLastLocation;
    private OBDService.OBDEventPID obdLastEvent;

    /**
     * Mají být jízdní události zaznamenány
     */
    private boolean tripRecordEnabled;

    /**
     * Inicializuje stav záznamu jízdy
     */
    private void initTripRecording(){
        tripRecordEnabled = false;
        resetTripRecording();
    }

    /**
     * Povolí záznam jízdy
     */
    public void enableTripRecording(){
        AppLog.i(AppLog.LOG_TAG_DB, "trip recording enabled");
        tripRecordEnabled = true;
        tripStart = System.currentTimeMillis();
    }

    /**
     * Zakáže záznam jízdy
     */
    public void disableTripRecording(){
        AppLog.i(AppLog.LOG_TAG_DB, "trip recording disabled");
        tripRecordEnabled = false;
        tripStop = System.currentTimeMillis();
    }

    /**
     * Smaže statistiky z jízdy
     * - po uložeí do souboru
     */
    public void resetTripRecording(){
        AppLog.i(AppLog.LOG_TAG_DB, "trip recording reset");
        seconds = 0;
        count = 0;
        gpsDistance = 0.0;
        obdDistance = 0.0;
        gpsLastLocation = null;
        obdLastEvent = null;
    }

    public void incrementGpsDistance(Location loc){
        if(gpsLastLocation != null){
            gpsDistance += gpsLastLocation.distanceTo(loc);
        }
        gpsLastLocation = loc;
    }

    private double metersPerMilisecConvert = 1.0 / 3600.0;
    public void incrementObdDistance(OBDService.OBDEventPID evt){

        if(obdLastEvent != null){
            long milisElapsed = evt.getTimeCreated() - obdLastEvent.getTimeCreated();
            obdDistance += metersPerMilisecConvert * evt.getValue() * milisElapsed;
        }

        obdLastEvent = evt;
    }

    /**
     * Zařadí do fronty příchozí GPS event
     * @param evt
     */
    @Handler
    public void handleLocationUpdate(ServiceGPS.GPSPositionEvent evt){
        //AppLog.i(AppLog.LOG_TAG_GPS, "DB got GPS event");
        queue.add(evt);
    }

    /**
     * Zařadí do fronty příchozí OBD event
     * @param evt
     */
    @Handler
    public void handleLocationUpdate(OBDService.OBDEventPID evt){

        // je povolen záznam?
        if(!tripRecordEnabled){ return; }

        // záznam
        queue.add(evt);
    }

    /**
     * Eventy z akcelerace
     * @param evt
     */
    @Handler
    public void handleAccelEvent(AccelService.AccelEvent evt){
        //AppLog.i(AppLog.LOG_TAG_GPS, "DB got GPS event");
        queue.add(evt);
    }

    /**
     * Zaznamená příchozí čas
     * @param evt
     */
    @Handler
    public void handleClockEvent(ClockService.TimeEvent evt){

        // debug
        //AppLog.i(AppLog.LOG_TAG_DB, "time incomin");

        // je povolen záznam?
        if(!tripRecordEnabled){ return; }

        // inkrementujeme čas
        // - nepředpokládáme race condition, neboť event příchází jen jednou za 1s
        seconds++;

        // debug
        //AppLog.i(AppLog.LOG_TAG_DB, "sending time event");

        // odešleme event
        ServiceManager.getInstance().eventBus.post(
                new DBEvent(count, seconds, obdDistance, gpsDistance)
        ).asynchronously();
    }


    // ---------- Hlavní cyklus ------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Uloží událost do databáze
     * @param evt Příchozí událost
     */
    public void storeTripMessage(ServiceManager.AppEvent evt){

        // delegujeme na helper
        helper.storeTripMessage(evt);

        // nový počet záznam
        // - nikde jej nepoužívám, je výhodnější používat odhad
        // - dotaz chvíli trvá
        count++;
        //TripDetailObject.getNumberOfRecord();

        // odešleme event o přidání záznamu
        ServiceManager.getInstance().eventBus.post(
            new DBEvent(count, seconds, obdDistance, gpsDistance)
        ).asynchronously();
    }

    /**
     * Ukončí thread bezpečně
     */
    public void exit(){
        threadRun = false;
    }

    /**
     * Hlavní cyklus vlákna
     */
    @Override
    public void run() {

        while(threadRun){

            // pokud máme zprávy, zpracujeme je, jinak nemcháme vlákno usnout
            if(queue.size() > 0){
                try {
                    storeTripMessage(queue.take());
                } catch (InterruptedException e) {
                    AppLog.p(AppLog.LOG_TAG_DB, "Exception while reading msg from queue!");
                }
            }else{
                try{
                    this.wait();
                } catch(Exception e){
                    // nevadí
                }
            }

        }
        //
        AppLog.i(AppLog.LOG_TAG_DB, "Database Service exited LOOP");
    }

    public static class DBEvent extends ServiceManager.AppEvent{

        private long count;
        private int time;
        private double gpsDistance;
        private double obdDistance;

        public DBEvent(long count, int time, double obdDistance, double gpsDistance) {
            this.count = count;
            this.time = time;
            this.obdDistance = obdDistance;
            this.gpsDistance = gpsDistance;
        }

        public long getCount() { return count; }
        public int getTime(){ return time; }
        public double getGpsDistance() { return gpsDistance; }
        public double getObdDistance() { return obdDistance; }

        @Override
        public int getType() {
            return ServiceManager.AppEvent.EVENT_DB;
        }
    }

    // ---------- PERSISTENCE NASTAVENÍ ----------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    private final String SHARED_PREFS_NAME = "appSettings";

    /**
     * Vrátí objekt nastavení, je možné z něj přímo číst
     * @return SharedPreferences
     */
    public SharedPreferences getSettings(){
        return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_MULTI_PROCESS);
    }

    /**
     * Vrátí editor nastavení, nezapomente volat commit po provedení změn
     * @return Editor nasatvení, je možné řetězit zápisy
     */
    public SharedPreferences.Editor editSettings(){
        return getSettings().edit();
    }

    /**
     * Vymaže všechna nastavení (!)
     */
    public void eraseSettings(){
        getSettings().edit().clear().commit();
    }
}

