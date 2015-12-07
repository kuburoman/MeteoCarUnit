package cz.meteocar.unit.engine.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import org.json.JSONObject;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.accel.AccelService;
import cz.meteocar.unit.engine.enums.RecordTypeEnum;
import cz.meteocar.unit.engine.gps.ServiceGPS;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.obd.OBDService;
import cz.meteocar.unit.engine.storage.model.FileObject;
import cz.meteocar.unit.engine.storage.model.ObdPidObject;
import cz.meteocar.unit.engine.storage.model.RecordEntity;
import cz.meteocar.unit.engine.storage.service.RecordService;

/**
 * Created by Toms, 2014.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private RecordService recordService;

    public DatabaseHelper(Context context) {
        super(context, MySQLiteConfig.DATABASE_NAME, null, MySQLiteConfig.DATABASE_VERSION);
        recordService = new RecordService();
        AppLog.i(AppLog.LOG_TAG_DB, "TripDetails Constructor");
    }

    /**
     * Uloží událost do databáze
     * - slouží pro asynchronní záznam jízdních dat
     *
     * @param evt Příchozí událost
     */
    public void storeTripMessage(ServiceManager.AppEvent evt) {

        // nový objekt
        boolean canWrite = false;
        RecordEntity obj = new RecordEntity();

        // vložíme rovnou čas
        obj.setTime(evt.getTimeCreated());

        // připravíme JSON objekt na data
        JSONObject jsonObj = new JSONObject();

        // zapíšeme GPS event
        if (evt.getType() == ServiceManager.AppEvent.EVENT_GPS_POSITION) {
            canWrite = true;

            // vložíme gps type
            obj.setType(RecordTypeEnum.TYPE_GPS.getValue());

            // uložíme si lokaci
            Location loc = ((ServiceGPS.GPSPositionEvent) evt).getLocation();
            if (loc == null) {
                return;
            }

            // zapíšeme ji do objektu
            double m = 1000000.0;
            double k = 1000.0;
            try {
                jsonObj.put("lat", m * loc.getLatitude());
                jsonObj.put("long", m * loc.getLongitude());
                jsonObj.put("alt", loc.getAltitude());
                jsonObj.put("acc", loc.getAccuracy());
                jsonObj.put("speed", 3.6 * loc.getSpeed());
            } catch (Exception e) {
                AppLog.p(AppLog.LOG_TAG_DB, "Exception while adding GPS event data to JSON object");
            }

            // přidáme JSON objekt do DB záznamu
            obj.setJson(jsonObj.toString());

            // inkremetujeme
            ServiceManager.getInstance().db.incrementGpsDistance(loc);
        }

        // zapíšeme OBD event
        if (evt.getType() == ServiceManager.AppEvent.EVENT_OBD_PID) {
            canWrite = true;

            // OBD event
            OBDService.OBDEventPID obdEvent = (OBDService.OBDEventPID) evt;
            //AppLog.i(AppLog.LOG_TAG_DB, "DB OBD Event type is: "+obdEvent.getType());

            // vložíme type / tag
            obj.setType(obdEvent.getMessage().getTag());

            // přidáme hodnotu jako json
            try {
                canWrite = true;
                jsonObj.put(obdEvent.getMessage().getTag(), obdEvent.getValue());
            } catch (Exception e) {
                AppLog.p(AppLog.LOG_TAG_DB, "Exception while adding OBD event data to JSON object");
            }

            // přidáme json k záznamu
            obj.setJson(jsonObj.toString());

            // inkremetujeme
            if (obdEvent.getMessage().getID() == ObdPidObject.OBD_PID_ID_SPEED) {
                ServiceManager.getInstance().db.incrementObdDistance(obdEvent);
            }
        }

        // zapíšeme akcelerační event
        if (evt.getType() == ServiceManager.AppEvent.EVENT_ACCEL) {
            canWrite = true;

            // accel event
            AccelService.AccelEvent accelEvent = (AccelService.AccelEvent) evt;

            // vložíme type / tag
            obj.setType(RecordTypeEnum.TYPE_ACCEL.getValue());

            // přidáme hodnotu jako json
            try {
                jsonObj.put("x", accelEvent.getX());
                jsonObj.put("y", accelEvent.getY());
                jsonObj.put("z", accelEvent.getZ());
                jsonObj.put("total", accelEvent.getValue());
            } catch (Exception e) {
                AppLog.p(AppLog.LOG_TAG_DB, "Exception while adding OBD event data to JSON object");
            }

            // přidáme json k záznamu
            obj.setJson(jsonObj.toString());
        }

        // mám něco k zaznamenání?
        if (!canWrite) {
            return;
        }

        // uložíme
        if (recordService.save(obj) < 1) {
            AppLog.p(AppLog.LOG_TAG_DB, "Trip event detail not saved");
        }
        ;
    }

    // ---------- Create, Up & Downgrade  --------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Vytvoření databáze
     * - voláno automaticky
     *
     * @param db Zapisovatelná SqLite DB
     */
    public void onCreate(SQLiteDatabase db) {
        AppLog.i(AppLog.LOG_TAG_DB, "DB onCreate");
        db.execSQL(RecordService.SQL_CREATE_ENTRIES);
        db.execSQL(ObdPidObject.SQL_CREATE_ENTRIES);
        db.execSQL(FileObject.SQL_CREATE_ENTRIES);
    }

    /**
     * Update databáze
     * - voláno automaticky (pokud nesedí aktuální verze DB s verzí v konfiuraci)
     *
     * @param db Zapisovatelná SqLite DB
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(RecordService.SQL_DELETE_ENTRIES);
        db.execSQL(ObdPidObject.SQL_DELETE_ENTRIES);
        db.execSQL(FileObject.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * Update databáze na nižší verzy
     * - voláno automaticky (pokud nesedí aktuální verze DB s verzí v konfiuraci)
     *
     * @param db Zapisovatelná SqLite DB
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
