package cz.meteocar.unit.engine.storage;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.accel.AccelService;
import cz.meteocar.unit.engine.gps.ServiceGPS;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.obd.OBDService;
import cz.meteocar.unit.engine.storage.model.FileObject;
import cz.meteocar.unit.engine.storage.model.ObdPidObject;
import cz.meteocar.unit.engine.storage.model.TripDetailObject;

/**
 * Created by Toms, 2014.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, MySQLiteConfig.DATABASE_NAME, null, MySQLiteConfig.DATABASE_VERSION);
        AppLog.i(AppLog.LOG_TAG_DB, "TripDetails Constructor");
    }

    /**
     * Uloží událost do databáze
     * - slouží pro asynchronní záznam jízdních dat
     * @param evt Příchozí událost
     */
    public void storeTripMessage(ServiceManager.AppEvent evt){

        // nový objekt
        boolean canWrite = false;
        TripDetailObject obj = new TripDetailObject();

        // vložíme rovnou časn
        obj.setTime(evt.getTimeCreated());

        // připravíme JSON objekt na data
        JSONObject jsonObj = new JSONObject();

        // zapíšeme GPS event
        if(evt.getType() == ServiceManager.AppEvent.EVENT_GPS_POSITION){
            canWrite = true;

            // vložíme gps type
            obj.setType(TripDetailObject.TYPE_GPS);

            // uložíme si lokaci
            Location loc = ((ServiceGPS.GPSPositionEvent)evt).getLocation();
            if(loc == null){return;}

            // zapíšeme ji do objektu
            double m = 1000000.0;
            double k = 1000.0;
            try {
                //jsonObj.put(TripDetailObject.COLUMN_NAME_TYPE, TripDetailObject.TYPE_GPS);
                jsonObj.put("lat", m * loc.getLatitude());
                jsonObj.put("long", m * loc.getLongitude());
                jsonObj.put("alt", loc.getAltitude());
                jsonObj.put("acc", loc.getAccuracy());
                jsonObj.put("speed", 3.6*loc.getSpeed());
            }catch (Exception e){
                AppLog.p(AppLog.LOG_TAG_DB, "Exception while adding GPS event data to JSON object");
            }

            // přidáme JSON objekt do DB záznamu
            obj.setJson(jsonObj.toString());

            // inkremetujeme
            ServiceManager.getInstance().db.incrementGpsDistance(loc);
        }

        // zapíšeme OBD event
        if(evt.getType() == ServiceManager.AppEvent.EVENT_OBD_PID){
            canWrite = true;

            // OBD event
            OBDService.OBDEventPID obdEvent = (OBDService.OBDEventPID)evt;
            //AppLog.i(AppLog.LOG_TAG_DB, "DB OBD Event type is: "+obdEvent.getType());

            // vložíme type / tag
            obj.setType(obdEvent.getMessage().getTag());

            // přidáme hodnotu jako json
            try{
                canWrite = true;
                jsonObj.put(obdEvent.getMessage().getTag(), obdEvent.getValue());
            }catch (Exception e){
                AppLog.p(AppLog.LOG_TAG_DB, "Exception while adding OBD event data to JSON object");
            }

            // přidáme json k záznamu
            obj.setJson(jsonObj.toString());

            // inkremetujeme
            if(obdEvent.getMessage().getID() == ObdPidObject.OBD_PID_ID_SPEED) {
                ServiceManager.getInstance().db.incrementObdDistance(obdEvent);
            }
        }

        // zapíšeme akcelerační event
        if(evt.getType() == ServiceManager.AppEvent.EVENT_ACCEL){
            canWrite = true;

            // accel event
            AccelService.AccelEvent accelEvent = (AccelService.AccelEvent) evt;

            // vložíme type / tag
            obj.setType(TripDetailObject.TYPE_ACCEL);

            // přidáme hodnotu jako json
            try{
                jsonObj.put("x", accelEvent.getX());
                jsonObj.put("y", accelEvent.getY());
                jsonObj.put("z", accelEvent.getZ());
                jsonObj.put("total", accelEvent.getValue());
            }catch (Exception e){
                AppLog.p(AppLog.LOG_TAG_DB, "Exception while adding OBD event data to JSON object");
            }

            // přidáme json k záznamu
            obj.setJson(jsonObj.toString());
        }

        // mám něco k zaznamenání?
        if(!canWrite){ return; }

        // uložíme
        if(TripDetailObject.save(obj) < 1){
            AppLog.p(AppLog.LOG_TAG_DB, "Trip event detail not saved");
        };
    }

    /**
     * Uloží záznamy do souboru
     * @return
     */
    public File saveToFile(){

        // otevřít DB a vytvořit kurzor
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TripDetailObject.TABLE_NAME, null);

        // vytvořit JSON ze záznamů
        JSONObject root = new JSONObject();
        JSONArray arrData = new JSONArray();
        JSONArray arrHead = new JSONArray();
        try {

            // načteme záznamy
            JSONObject newObj;
            String col;
            int objIndex = 0;
            if (cursor .moveToFirst()) {
                while (cursor.isAfterLast() == false) {

                    // přepíšeme záznamy z db do JSONu
                    newObj = new JSONObject();
                    newObj.put(TripDetailObject.COLUMN_NAME_ID, cursor.getString(cursor.getColumnIndex(TripDetailObject.COLUMN_NAME_ID)));
                    newObj.put(TripDetailObject.COLUMN_NAME_TYPE, cursor.getString(cursor.getColumnIndex(TripDetailObject.COLUMN_NAME_TYPE)));
                    newObj.put(TripDetailObject.COLUMN_NAME_JSON, cursor.getString(cursor.getColumnIndex(TripDetailObject.COLUMN_NAME_JSON)));
                    newObj.put(TripDetailObject.COLUMN_NAME_TIME, cursor.getString(cursor.getColumnIndex(TripDetailObject.COLUMN_NAME_TIME)));

                    // zapíšeme do pole
                    arrData.put(objIndex++, newObj);

                    // další řádek
                    cursor.moveToNext();
                }
            }

            // hlavička - načteme pidy
            cursor = db.rawQuery("SELECT * FROM " + ObdPidObject.TABLE_NAME
                    + " WHERE " + ObdPidObject.COLUMN_NAME_ACTIVE + " = 1", null);
            objIndex = 0;
            if (cursor .moveToFirst()) {
                while (cursor.isAfterLast() == false) {

                    // přepíšeme záznamy z db do JSONu
                    newObj = new JSONObject();
                    newObj.put(ObdPidObject.COLUMN_NAME_ID, cursor.getString(cursor.getColumnIndex(ObdPidObject.COLUMN_NAME_ID)));
                    newObj.put(ObdPidObject.COLUMN_NAME_NAME, cursor.getString(cursor.getColumnIndex(ObdPidObject.COLUMN_NAME_NAME)));
                    newObj.put(ObdPidObject.COLUMN_NAME_TAG, cursor.getString(cursor.getColumnIndex(ObdPidObject.COLUMN_NAME_TAG)));
                    newObj.put(ObdPidObject.COLUMN_NAME_PID_CODE, cursor.getString(cursor.getColumnIndex(ObdPidObject.COLUMN_NAME_PID_CODE)));
                    newObj.put(ObdPidObject.COLUMN_NAME_FORMULA, cursor.getString(cursor.getColumnIndex(ObdPidObject.COLUMN_NAME_FORMULA)));
                    newObj.put(ObdPidObject.COLUMN_NAME_LOCKED, cursor.getString(cursor.getColumnIndex(ObdPidObject.COLUMN_NAME_LOCKED)));

                    // zapíšeme do pole
                    arrHead.put(objIndex++, newObj);

                    // další řádek
                    cursor.moveToNext();
                }
            }

            // detaily jízdy
            JSONObject infoObj = new JSONObject();
            infoObj.put("start", ServiceManager.getInstance().db.getTripStart());
            infoObj.put("stop", ServiceManager.getInstance().db.getTripStop());
            infoObj.put("time", ServiceManager.getInstance().db.getTripTime());
            infoObj.put("distance", ServiceManager.getInstance().db.getTripDistance());


            root.put("TripDetailsInfo", infoObj);
            root.put("TripDetailsHead", arrHead);
            root.put("TripDetailsLog", arrData);
        } catch (Exception e){
            //
            AppLog.p("JSON Exception while saving trip log to file");
            return null;
        }

        // --- datum
        SimpleDateFormat dateF = new SimpleDateFormat("yyyy_MM_dd");
        SimpleDateFormat timeF = new SimpleDateFormat("HH_mm_ss");
        Date now = new Date();
        String strDate = dateF.format(now);
        String strTime = timeF.format(now);

        // --- zapíšeme do souboru v interní paměti zařízení
        // připravíme název a umístění
        String filename = "trip" + "[" + strDate + "]" + "[" + strTime + "]" + ".json";
        //File file = new File(ServiceManager.getInstance().getContext().getFilesDir(), "tripDetailsLog.json");
        File file = new File(FileSystem.getTripLogDir(), filename);
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            return null;
        }
        AppLog.i("DB FILE abs path: "+file.getAbsolutePath());

        // zápis
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(root.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // --- zapíšeme do db
        FileObject fo = new FileObject();
        fo.setFilename(file.getAbsolutePath());
        fo.setType(FileObject.TYPE_TRIP_DETAILS);
        fo.setTime(now.getTime());
        fo.setServerID(-1);
        FileObject.save(fo);

        return file;
    }

    /**
     * Pokusí se odelsta na server všechny soubory
     */
    public void sendAllToServer(){

    }


    // ---------- Create, Up & Downgrade  --------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Vytvoření databáze
     * - voláno automaticky
     * @param db Zapisovatelná SqLite DB
     */
    public void onCreate(SQLiteDatabase db) {
        AppLog.i(AppLog.LOG_TAG_DB, "DB onCreate");
        db.execSQL(TripDetailObject.SQL_CREATE_ENTRIES);
        db.execSQL(ObdPidObject.SQL_CREATE_ENTRIES);
        db.execSQL(FileObject.SQL_CREATE_ENTRIES);
    }

    /**
     * Update databáze
     * - voláno automaticky (pokud nesedí aktuální verze DB s verzí v konfiuraci)
     * @param db Zapisovatelná SqLite DB
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TripDetailObject.SQL_DELETE_ENTRIES);
        db.execSQL(ObdPidObject.SQL_DELETE_ENTRIES);
        db.execSQL(FileObject.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * Update databáze na nižší verzy
     * - voláno automaticky (pokud nesedí aktuální verze DB s verzí v konfiuraci)
     * @param db Zapisovatelná SqLite DB
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
