package cz.meteocar.unit.engine;

import android.content.Context;
import android.util.Log;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;

import cz.meteocar.unit.engine.accel.AccelService;
import cz.meteocar.unit.engine.clock.ClockService;
import cz.meteocar.unit.engine.gps.ServiceGPS;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.network.NetworkService;
import cz.meteocar.unit.engine.obd.OBDService;
import cz.meteocar.unit.engine.storage.ConvertService;
import cz.meteocar.unit.engine.storage.DatabaseService;

/**
 * Created by Toms, 2014.
 */
public class ServiceManager {

    // verze
    public final String version = "1.15";

    // singleton pattern
    private static final ServiceManager MY_SERVICE_MANAGER = new ServiceManager();
    public static ServiceManager getInstance() {
        return MY_SERVICE_MANAGER;
    }

    // kontext aplikace
    private Context context;
    public Context getContext(){return context;}

    // služby
    public ClockService clock;
    public ServiceGPS gps;
    public OBDService obd;
    public DatabaseService db;
    public NetworkService network;
    public AccelService accel;
    public ConvertService convert;

    // bus
    public MBassador<AppEvent> eventBus;

    /**
     * Inicializace manageru, start služeb
     */
    public void init(Context baseContext){

        // context
        context = baseContext;

        // bus
        eventBus = new MBassador<>(BusConfiguration.SyncAsync());

        // vytvoření a spuštění služeb
        clock = new ClockService();
        gps = new ServiceGPS(context);
        obd = new OBDService(context);
        db = new DatabaseService(context);
        network = new NetworkService(context);
        accel = new AccelService(context);
        convert = new ConvertService();
    }

    /**
     * Bezpečně ukončí služby
     */
    public void exitServices(){

        // nastavíme všem službám ukončovací flag
        clock.exit();
        gps.exit();
        obd.exit();
        db.exit();
        network.exit();
        convert.exit();

        // teď jen voláme na jejich vláknech joiny
        try{
            clock.join(500);
        }catch(InterruptedException e){
            Log.e(AppLog.LOG_TAG_DEFAULT, "Clock thread interrupted for calling join.",e);
        }
        try{
            gps.join(500);
        }catch(InterruptedException e){
            Log.e(AppLog.LOG_TAG_DEFAULT, "GPS thread interrupted for calling join.",e);
        }
        try{
            obd.join(500);
        }catch(InterruptedException e){
            Log.e(AppLog.LOG_TAG_DEFAULT, "OBD thread interrupted for calling join.",e);
        }
        try{
            db.join(500);
        }catch(InterruptedException e){
            Log.e(AppLog.LOG_TAG_DEFAULT, "DB thread interrupted for calling join.",e);
        }
        try{
            network.join(500);
        }catch(InterruptedException e){
            Log.e(AppLog.LOG_TAG_DEFAULT, "Network thread interrupted for calling join.",e);
        }

        // nastavíme na null, pro jistotu
        gps = null;
        obd = null;
        db = null;
        network = null;
        convert = null;

    }

    /**
     * Ukončí služby i aplikaci
     */
    public void exitApp(){
        exitServices();
        System.exit(0);
    }

    /**
     * Vnitřní třída představující změnu stavu služby
     */
    abstract public static class AppEvent {
        public static final int EVENT_GPS_POSITION = 0;
        public static final int EVENT_GPS_STATUS = 1;
        public static final int EVENT_OBD_PID = 2;
        public static final int EVENT_OBD_STATUS = 3;
        public static final int EVENT_CLOCK = 4;
        public static final int EVENT_POWER_STATUS = 5;
        public static final int EVENT_NFC = 6;
        public static final int EVENT_VIDEO = 7;
        public static final int EVENT_ACCEL = 8;
        public static final int EVENT_DB = 9;
        public static final int EVENT_NETWORK = 10;

        private String userId;
        private String tripId;

        /**
         * Čas vytvoření eventu
         */
        protected Long timeCreated;

        /**
         * Super-konstruktor inicializující čas vytvoření
         */
        public AppEvent(){
            timeCreated = System.currentTimeMillis();
        }

        /**
         * Vrací čas vytvoření eventu
         * @return Časová známka
         */
        public Long getTimeCreated(){
            return timeCreated;
        }

        /**
         * Vrací typ "globální" typ eventu
         * @return Typ eventu
         */
        abstract public int getType();

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getTripId() {
            return tripId;
        }

        public void setTripId(String tripId) {
            this.tripId = tripId;
        }

    }
}
