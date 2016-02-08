package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.accel.AccelService;
import cz.meteocar.unit.engine.enums.RecordTypeEnum;
import cz.meteocar.unit.engine.gps.ServiceGPS;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.obd.OBDService;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.TripDetailVO;
import cz.meteocar.unit.engine.storage.model.RecordEntity;

/**
 * Servisa starajici se o vsechny zaznamy z jizdy
 */
public class RecordHelper {

    /* Definice obsahu DB tabulky */
    public static final String TABLE_NAME = "record_details";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_TIME = "time";
    public static final String COLUMN_NAME_USER_ID = "user_id";
    public static final String COLUMN_NAME_TRIP_ID = "trip_id";
    public static final String COLUMN_NAME_TYPE = "type";
    public static final String COLUMN_NAME_JSON = "json";
    public static final String COLUMN_NAME_PROCESSED = "processed";

    /* SQL statement pro vytvoreni tabulky */
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + MySQLiteConfig.TYPE_ID + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_TIME + MySQLiteConfig.TYPE_INTEGER + " DEFAULT 0" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_USER_ID + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_TRIP_ID + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_TYPE + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_JSON + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_PROCESSED + MySQLiteConfig.TYPE_BOOLEAN + " DEFAULT ''" +
                    " )";

    /* SQL statement pro smazani tabulky */
    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String SQL_GET_ALL = "SELECT  * FROM " + TABLE_NAME;

    /**
     * Nacte vsechny zaznamy
     *
     * @return ArrayList vsech objektu
     */
    public ArrayList<RecordEntity> getAll() {

        //
        ArrayList<RecordEntity> arr = new ArrayList<>();

        // pripravime kurzor k DB
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        // projdeme po radcich
        RecordEntity obj;
        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {

                obj = createEntity(cursor);
                arr.add(obj);

                // dalsi
                cursor.moveToNext();
            }
        }

        // ok
        return arr;
    }

    /**
     * Vlozeni noveho objektu
     *
     * @param obj Vkladany objekt
     * @return Pocet ovlivnenych radek
     */
    public int save(RecordEntity obj) {

        // nové values
        ContentValues values = new ContentValues();

        // nastavíme hodnoty
        values.put(COLUMN_NAME_TIME, obj.getTime());
        values.put(COLUMN_NAME_TYPE, obj.getType());
        values.put(COLUMN_NAME_USER_ID, obj.getUserName());
        values.put(COLUMN_NAME_TRIP_ID, obj.getTripId());
        values.put(COLUMN_NAME_JSON, obj.getJson());
        values.put(COLUMN_NAME_PROCESSED, obj.isProcessed());

        // db
        SQLiteDatabase db = DB.helper.getWritableDatabase();

        // vložíme nebo updatujeme v závislosti na ID
        if (obj.getId() > 0) {

            // máme id, provedeme update
            values.put(COLUMN_NAME_ID, obj.getId());
            return (int) db.update(TABLE_NAME, values, "id = ?", new String[]{"" + obj.getId()});
        } else {

            // nemáme íd, vložíme nový záznam
            return (int) db.insert(TABLE_NAME, null, values);    // nepředpokládáme přetečení int
        }
    }

    /**
     * Ziskani objektu z DB dle ID
     *
     * @param id ID objektu
     * @return True - pokud se podarilo objekt nalazt, False - pokud ne
     */
    public RecordEntity get(int id) {

        SQLiteDatabase db = DB.helper.getReadableDatabase();

        Cursor c = db.query(TABLE_NAME, null, "id = ?", new String[]{"" + id}, null, null, null);

        if (c.getCount() > 0) {
            c.moveToFirst();

            RecordEntity obj = createEntity(c);
            return obj;

        } else {
            return null;
        }
    }

    public List<RecordEntity> getByUserId(String userId, int maxNumberOfRecords, boolean processed) {

        RecordEntity obj;
        List<RecordEntity> arr = new ArrayList<>();

        SQLiteDatabase db = DB.helper.getReadableDatabase();

        Cursor cs = db.query(TABLE_NAME, null, COLUMN_NAME_USER_ID + " = ? and " + COLUMN_NAME_PROCESSED + " = ?", new String[]{userId, processed == false ? "0" : "1"}, null, null, null, String.valueOf(maxNumberOfRecords));

        if (cs.moveToFirst()) {
            while (cs.isAfterLast() == false) {

                obj = createEntity(cs);
                arr.add(obj);

                // dalsi
                cs.moveToNext();
            }
        }

        return arr;
    }

    /**
     * Vrati pocet radku tabulky
     *
     * @return Pocet radku
     */
    public int getNumberOfRecord() {

        SQLiteDatabase db = DB.helper.getReadableDatabase();

        Cursor cursor = db.rawQuery(SQL_GET_ALL, null);
        int cnt = cursor.getCount();
        cursor.close();

        //
        return cnt;
    }

    public int getNumberOfRecord(Boolean processed) {
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, COLUMN_NAME_PROCESSED + " = ?", new String[]{processed == false ? "0" : "1"}, null, null, null);
        int count = c.getCount();
        c.close();

        return count;
    }

    /**
     * Smaze vsechny zaznamy z tabulky
     */
    public void deleteAllRecords() {
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    public void deleteRecords(List<Integer> id) {
        String[] array = new String[id.size()];

        for (int i = 0; i < id.size(); i++) {
            array[i] = String.valueOf(id.get(i));
        }

        SQLiteDatabase db = DB.helper.getReadableDatabase();
        db.delete(TABLE_NAME, "id IN (" + makePlaceholders(id.size()) + ")", array);
    }

    public void updateProcessed(List<Integer> id, Boolean processed) {
        String[] array = new String[id.size()];


        for (int i = 0; i < id.size(); i++) {
            array[i] = String.valueOf(id.get(i));
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_PROCESSED, processed);

        SQLiteDatabase db = DB.helper.getReadableDatabase();
        db.update(TABLE_NAME, values, "id IN (" + makePlaceholders(id.size()) + ")", array);
    }

    protected String makePlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    public ArrayList<TripDetailVO> getUserTripDetailList(String userId) {
        ArrayList<TripDetailVO> detailVOs = new ArrayList<>();
        List<String> userTrips = getUserTrips(userId);
        for (String tripId : userTrips) {
            Long startTime = getTimeOfTrip(tripId, true);
            Long endTime = getTimeOfTrip(tripId, false);
            detailVOs.add(new TripDetailVO(tripId, startTime, endTime));
        }
        return detailVOs;
    }

    protected List<String> getUserTrips(String userId) {


        SQLiteDatabase db = DB.helper.getReadableDatabase();
        List<String> tripIds = new ArrayList<>();
        Cursor cursor = db.query(true, TABLE_NAME, new String[]{COLUMN_NAME_TRIP_ID}, COLUMN_NAME_USER_ID + " = ?", new String[]{userId}, null, null, null, null);

        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {

                tripIds.add(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TRIP_ID)));

                cursor.moveToNext();
            }
        }
        return tripIds;
    }

    protected Long getTimeOfTrip(String tripId, boolean min) {
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        Cursor cursor;
        if (min) {
            cursor = db.query(TABLE_NAME, null, COLUMN_NAME_TRIP_ID + " = ?", new String[]{tripId}, null, null, COLUMN_NAME_TIME + " ASC", "1");
        } else {
            cursor = db.query(TABLE_NAME, null, COLUMN_NAME_TRIP_ID + " = ?", new String[]{tripId}, null, null, COLUMN_NAME_TIME + " DESC", "1");
        }

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Long time = cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_TIME));
            cursor.close();
            return time;

        }
        return null;
    }


    public List<String> getUserIdStored() {
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        List<String> userIds = new ArrayList<>();
        Cursor cursor = db.query(true, TABLE_NAME, new String[]{COLUMN_NAME_USER_ID}, null, null, COLUMN_NAME_ID, null, null, null);

        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {

                userIds.add(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_USER_ID)));

                cursor.moveToNext();
            }
        }
        return userIds;
    }

    public RecordEntity createEntity(Cursor cursor) {
        RecordEntity obj = new RecordEntity();
        obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
        obj.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_TIME)));
        obj.setTripId(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TRIP_ID)));
        obj.setUserName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_USER_ID)));
        obj.setType(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TYPE)));
        obj.setJson(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_JSON)));
        obj.setProcessed(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_PROCESSED)) != 0);
        return obj;
    }

    public void save(ServiceManager.AppEvent evt) {

        // nový objekt
        boolean canWrite = false;
        RecordEntity obj = new RecordEntity();

        // vložíme rovnou čas
        obj.setTime(evt.getTimeCreated());
        obj.setUserName(evt.getUserId());
        obj.setTripId(evt.getTripId());
        obj.setProcessed(false);


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
                jsonObj.put("lng", m * loc.getLongitude());
                jsonObj.put("alt", loc.getAltitude());
                jsonObj.put("acc", loc.getAccuracy());
                // TODO - Až bude přidaná rychlost odkomentovat
                //  jsonObj.put("speed", 3.6 * loc.getSpeed());
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
            //AppLog.i(AppLog.LOG_TAG_DB, "DB OBD Event type is: "+obdEvent.getObdCode());

            // vložíme type / tag
            obj.setType(obdEvent.getMessage().getTag());

            // přidáme hodnotu jako json
            try {
                canWrite = true;
                jsonObj.put("value", obdEvent.getValue());
            } catch (Exception e) {
                AppLog.p(AppLog.LOG_TAG_DB, "Exception while adding OBD event data to JSON object");
            }

            // přidáme json k záznamu
            obj.setJson(jsonObj.toString());

            // inkremetujeme
            if (obdEvent.getMessage().getID() == ObdPidHelper.OBD_PID_ID_SPEED) {
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
//                jsonObj.put("total", accelEvent.getValue());
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
        if (save(obj) < 1) {
            AppLog.p(AppLog.LOG_TAG_DB, "Trip event detail not saved");
        }

    }

}
