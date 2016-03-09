package cz.meteocar.unit.engine.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import net.engio.mbassy.listener.Handler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.accel.event.AccelerationEvent;
import cz.meteocar.unit.engine.clock.event.TimeEvent;
import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.gps.event.GPSPositionEvent;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.obd.event.OBDPidEvent;
import cz.meteocar.unit.engine.storage.event.DBEvent;
import cz.meteocar.unit.engine.storage.helper.DatabaseHelper;
import cz.meteocar.unit.engine.storage.helper.FilterSettingHelper;
import cz.meteocar.unit.engine.storage.helper.ObdPidHelper;
import cz.meteocar.unit.engine.storage.helper.RecordHelper;
import cz.meteocar.unit.engine.storage.helper.TripHelper;
import cz.meteocar.unit.engine.storage.helper.UserHelper;

/**
 * Created by Toms, 2014.
 */
public class DatabaseService extends Thread {

    /**
     * Fronta na eventy
     * - zápisy do DB mohou chvíli trvat, proto je žádoucí je spuštět až ve vlastním vlákně,
     * které bude číst eventy z této fronty
     */
    BlockingQueue<AppEvent> queue;

    private boolean threadRun;
    private Context context;


    // helper
    private DatabaseHelper helper;
    private TripHelper tripHelper;
    private RecordHelper recordHelper;
    private UserHelper userHelper;
    private ObdPidHelper obdPidHelper;
    private FilterSettingHelper filterSettingHelper;


    public DatabaseService(Context ctx) {
        context = ctx;

        // nová fronta na eventy
        queue = new ArrayBlockingQueue<AppEvent>(20);

        // helper
        helper = new DatabaseHelper(ctx);
        tripHelper = new TripHelper(helper);
        userHelper = new UserHelper(helper);
        obdPidHelper = new ObdPidHelper(helper);
        filterSettingHelper = new FilterSettingHelper(helper);
        recordHelper = new RecordHelper(helper, filterSettingHelper);
        // přihlášení k odběru dat ze service busu
        ServiceManager.getInstance().eventBus.subscribe(this);

        // inicializujeme stav zaznamenávání jízdy
        initTripRecording();

        // start threadu
        threadRun = true;
        start();
    }

    // ---------- Záznam z jízdy -----------------------------------------------------------------
    // -------------------------------------------------------------------------------------------


    public DatabaseHelper getHelper() {
        return helper;
    }

    public void setHelper(DatabaseHelper helper) {
        this.helper = helper;
    }

    public void setTripHelper(TripHelper tripHelper) {
        this.tripHelper = tripHelper;
    }

    public void setRecordHelper(RecordHelper recordHelper) {
        this.recordHelper = recordHelper;
    }

    public void setUserHelper(UserHelper userHelper) {
        this.userHelper = userHelper;
    }

    public void setObdPidHelper(ObdPidHelper obdPidHelper) {
        this.obdPidHelper = obdPidHelper;
    }

    public void setFilterSettingHelper(FilterSettingHelper filterSettingHelper) {
        this.filterSettingHelper = filterSettingHelper;
    }

    private int seconds;

    public int getTripTime() {
        return seconds;
    }

    private long count;
    private double gpsDistance;
    private double obdDistance;

    public double getTripDistance() {
        return obdDistance;
    }

    private long tripStart;

    public long getTripStart() {
        return tripStart;
    }

    private long tripStop;

    public long getTripStop() {
        return tripStop;
    }

    private Location gpsLastLocation;
    private OBDPidEvent obdLastEvent;
    private String userName;
    private String tripId;

    /**
     * Mají být jízdní události zaznamenány
     */
    private boolean tripRecordEnabled;

    /**
     * Inicializuje stav záznamu jízdy
     */
    private void initTripRecording() {
        tripRecordEnabled = false;
        resetTripRecording();
    }

    /**
     * Povolí záznam jízdy
     */
    public void enableTripRecording() {
        AppLog.i(AppLog.LOG_TAG_DB, "trip recording enabled");
        tripRecordEnabled = true;
        tripStart = System.currentTimeMillis();
        userName = DB.getLoggedUser();
        tripId = String.valueOf(System.currentTimeMillis());
    }

    /**
     * Zakáže záznam jízdy
     */
    public void disableTripRecording() {
        AppLog.i(AppLog.LOG_TAG_DB, "trip recording disabled");
        tripRecordEnabled = false;
        tripStop = System.currentTimeMillis();
    }

    /**
     * Smaže statistiky z jízdy
     * - po uložeí do souboru
     */
    public void resetTripRecording() {
        AppLog.i(AppLog.LOG_TAG_DB, "trip recording reset");
        seconds = 0;
        count = 0;
        gpsDistance = 0.0;
        obdDistance = 0.0;
        gpsLastLocation = null;
        obdLastEvent = null;
    }

    public void incrementGpsDistance(Location loc) {
        if (gpsLastLocation != null) {
            gpsDistance += gpsLastLocation.distanceTo(loc);
        }
        gpsLastLocation = loc;
    }

    public void incrementObdDistance(OBDPidEvent evt) {
        double metersPerMilisecConvert = 1.0 / 3600.0;

        if (obdLastEvent != null) {
            long milisElapsed = evt.getTimeCreated() - obdLastEvent.getTimeCreated();
            obdDistance += metersPerMilisecConvert * evt.getValue() * milisElapsed;
        }

        obdLastEvent = evt;
    }

    /**
     * Zařadí do fronty příchozí GPS event
     *
     * @param evt
     */
    @Handler
    public void handleLocationUpdate(GPSPositionEvent evt) {
        if (!tripRecordEnabled) {
            return;
        }

        queue.add(evt);
    }

    /**
     * Zařadí do fronty příchozí OBD event
     *
     * @param evt
     */
    @Handler
    public void handleLocationUpdate(OBDPidEvent evt) {
        if (!tripRecordEnabled) {
            return;
        }

        queue.add(evt);
    }

    /**
     * Eventy z akcelerace
     *
     * @param evt
     */
    @Handler
    public void handleAccelEvent(AccelerationEvent evt) {
        if (!tripRecordEnabled) {
            return;
        }

        queue.add(evt);
    }

    /**
     * Zaznamená příchozí čas
     *
     * @param evt
     */
    @Handler
    public void handleClockEvent(TimeEvent evt) {
        if (!tripRecordEnabled) {
            return;
        }

        seconds++;

        ServiceManager.getInstance().eventBus.post(
                new DBEvent(count, seconds, obdDistance, gpsDistance)
        ).asynchronously();
    }


    // ---------- Hlavní cyklus ------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Uloží událost do databáze
     *
     * @param evt Příchozí událost
     */
    public void storeTripMessage(AppEvent evt) {
        if (DB.getLoggedUser() == null) {
            return;
        }

        evt.setUserId(DB.getLoggedUser());
        evt.setTripId(tripId);

        // delegujeme na helper
        recordHelper.save(evt);

        // nový počet záznam
        // - nikde jej nepoužívám, je výhodnější používat odhad
        // - dotaz chvíli trvá
        count++;

        // odešleme event o přidání záznamu
        ServiceManager.getInstance().eventBus.post(
                new DBEvent(count, seconds, obdDistance, gpsDistance)
        ).asynchronously();
    }

    /**
     * Ukončí thread bezpečně
     */
    public void exit() {
        threadRun = false;
    }

    /**
     * Hlavní cyklus vlákna
     */
    @Override
    public void run() {

        while (threadRun) {

            // pokud máme zprávy, zpracujeme je, jinak nemcháme vlákno usnout
            if (queue.size() > 0) {
                try {
                    storeTripMessage(queue.take());
                } catch (InterruptedException e) {
                    AppLog.p(AppLog.LOG_TAG_DB, "Exception while reading msg from queue!");
                }
            } else {

                try {
                    this.sleep(200);
                } catch (Exception e) {
                    Log.e(AppLog.LOG_TAG_DB, "Error when sleep.", e);
                }
            }

        }
        //
        AppLog.i(AppLog.LOG_TAG_DB, "Database Service exited LOOP");
    }

    // ---------- PERSISTENCE NASTAVENÍ ----------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    private final String SHARED_PREFS_NAME = "appSettings";

    /**
     * Vrátí objekt nastavení, je možné z něj přímo číst
     *
     * @return SharedPreferences
     */
    public SharedPreferences getSettings() {
        return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_MULTI_PROCESS);
    }

    /**
     * Vrátí editor nastavení, nezapomente volat commit po provedení změn
     *
     * @return Editor nasatvení, je možné řetězit zápisy
     */
    public SharedPreferences.Editor editSettings() {
        return getSettings().edit();
    }

    public TripHelper getTripHelper() {
        return tripHelper;
    }

    public RecordHelper getRecordHelper() {
        return recordHelper;
    }

    public UserHelper getUserHelper() {
        return userHelper;
    }


    public ObdPidHelper getObdPidHelper() {
        return obdPidHelper;
    }

    public FilterSettingHelper getFilterSettingHelper() {
        return filterSettingHelper;
    }

}

