package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.accel.event.AccelerationEvent;
import cz.meteocar.unit.engine.enums.RecordTypeEnum;
import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.event.EventType;
import cz.meteocar.unit.engine.gps.event.GPSPositionEvent;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.obd.event.OBDPidEvent;
import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.TripDetailVO;
import cz.meteocar.unit.engine.storage.helper.filter.AccelerationVO;
import cz.meteocar.unit.engine.storage.model.RecordEntity;

/**
 * Helper for saving and loading {@link RecordEntity}.
 */
public class RecordHelper extends AbstractHelper<RecordEntity> {

    private static final String TABLE_NAME = "record_details";
    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_TIME = "time";
    private static final String COLUMN_NAME_USER_ID = "user_id";
    private static final String COLUMN_NAME_TRIP_ID = "trip_id";
    private static final String COLUMN_NAME_TYPE = "type";
    private static final String COLUMN_NAME_JSON = "json";
    private static final String COLUMN_NAME_PROCESSED = "processed";

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

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String SQL_GET_ALL = "SELECT  * FROM " + TABLE_NAME;

    public RecordHelper(DatabaseHelper helper) {
        super(helper);
    }

    @Override
    public int save(RecordEntity obj) {
        try {
            ContentValues values = new ContentValues();

            values.put(COLUMN_NAME_TIME, obj.getTime());
            values.put(COLUMN_NAME_TYPE, obj.getType());
            values.put(COLUMN_NAME_USER_ID, obj.getUserName());
            values.put(COLUMN_NAME_TRIP_ID, obj.getTripId());
            values.put(COLUMN_NAME_JSON, obj.getJson());
            values.put(COLUMN_NAME_PROCESSED, obj.isProcessed());

            return this.innerSave(obj.getId(), values);
        } catch (DatabaseException exception) {
            Log.e(AppLog.LOG_TAG_DB, exception.getMessage(), exception);
            return -1;
        }
    }

    /**
     * Return List of entities based on userid.
     *
     * @param userId             id of user
     * @param maxNumberOfRecords number of maximum records
     * @param processed
     * @return List of {@link RecordEntity}
     */
    public List<RecordEntity> getByUserId(String userId, int maxNumberOfRecords, boolean processed) {
        List<RecordEntity> arr = new ArrayList<>();

        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_USER_ID + " = ? and " + COLUMN_NAME_PROCESSED + " = ?", new String[]{userId, processed ? "1" : "0"}, null, null, null, String.valueOf(maxNumberOfRecords));

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                arr.add(convert(cursor));
                cursor.moveToNext();
            }
        }

        cursor.close();
        return arr;
    }

    /**
     * Return List of entities based on user Id and type of record.
     *
     * @param userId    id of user
     * @param type      type of record that we want.
     * @param processed if record was prepared for sending on server.
     * @return List of {@link RecordEntity}
     */
    public List<RecordEntity> getByUserIdAndType(String userId, String type, boolean processed) {
        List<RecordEntity> arr = new ArrayList<>();

        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_USER_ID + " = ? and " + COLUMN_NAME_TYPE + " =  ? and " + COLUMN_NAME_PROCESSED + " = ?", new String[]{userId, type, processed ? "1" : "0"}, null, null, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                arr.add(convert(cursor));
                cursor.moveToNext();
            }
        }

        cursor.close();
        return arr;
    }

    /**
     * Returns entities based on type and trip id.
     *
     * @param tripId id of trip
     * @param type   of record
     * @return List of {@link AccelerationVO}
     */
    public List<AccelerationVO> getTripByType(String tripId, String type) {
        List<AccelerationVO> arr = new ArrayList<>();

        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_TRIP_ID + " = ? and " + COLUMN_NAME_TYPE + " = ?", new String[]{tripId, type}, null, null, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                arr.add(convertToAcceleration(convert(cursor)));

                cursor.moveToNext();
            }
        }

        cursor.close();
        return arr;
    }


    protected AccelerationVO convertToAcceleration(RecordEntity entity) {
        AccelerationVO obj = new AccelerationVO();
        obj.setUserId(entity.getUserName());
        obj.setTripId(entity.getTripId());
        obj.setTime(entity.getTime());
        obj.setType(entity.getType());

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(entity.getJson());
            obj.setX(jsonObject.getDouble("x"));
            obj.setY(jsonObject.getDouble("y"));
            obj.setZ(jsonObject.getDouble("z"));
        } catch (JSONException e) {
            Log.e(AppLog.LOG_TAG_DB, "Cannot parse acceleration.", e);
        }

        return obj;
    }

    /**
     * Return number of processed records.
     *
     * @param processed If they are processed
     * @return number of records
     */
    public int getNumberOfRecord(Boolean processed) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_PROCESSED + " = ?", new String[]{!processed ? "0" : "1"}, null, null, null);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    /**
     * Sets records processed.
     *
     * @param id        List of id to process.
     * @param processed
     */
    public void updateProcessed(List<Integer> id, Boolean processed) {
        String[] array = new String[id.size()];


        for (int i = 0; i < id.size(); i++) {
            array[i] = String.valueOf(id.get(i));
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_PROCESSED, processed);

        SQLiteDatabase db = helper.getReadableDatabase();
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

    /**
     * Return list of trip details based on userId.
     *
     * @param userId id of user
     * @return List of {@link TripDetailVO}
     */
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
        if (userId == null) {
            return new ArrayList<>();
        }
        SQLiteDatabase db = helper.getReadableDatabase();
        List<String> tripIds = new ArrayList<>();
        Cursor cursor = db.query(true, TABLE_NAME, new String[]{COLUMN_NAME_TRIP_ID}, COLUMN_NAME_USER_ID + " = ?", new String[]{userId}, null, null, null, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {

                tripIds.add(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TRIP_ID)));

                cursor.moveToNext();
            }
        }
        cursor.close();
        return tripIds;
    }

    protected Long getTimeOfTrip(String tripId, boolean min) {
        SQLiteDatabase db = helper.getReadableDatabase();
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

    /**
     * Return list of Users that have records stored in database/
     *
     * @return id of users.
     */
    public List<String> getUserIdStored() {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<String> userIds = new ArrayList<>();
        Cursor cursor = db.query(true, TABLE_NAME, new String[]{COLUMN_NAME_USER_ID}, null, null, COLUMN_NAME_ID, null, null, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                userIds.add(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_USER_ID)));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return userIds;
    }

    public List<String> getRecordsDistinctTypesForUser(String userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<String> userIds = new ArrayList<>();
        Cursor cursor = db.query(true, TABLE_NAME, new String[]{COLUMN_NAME_USER_ID}, COLUMN_NAME_USER_ID + " = ?", new String[]{userId}, COLUMN_NAME_TYPE, null, null, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                userIds.add(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_USER_ID)));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return userIds;
    }

    public void deleteUserNullRecords() {
        SQLiteDatabase db = helper.getReadableDatabase();
        db.delete(getTableNameSQL(), COLUMN_NAME_USER_ID + " is NULL OR trim(" + COLUMN_NAME_USER_ID + ") = ''", null);
    }

    @Override
    protected RecordEntity convert(Cursor cursor) {
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

    @Override
    protected String getAllSQL() {
        return SQL_GET_ALL;
    }

    @Override
    protected String getTableNameSQL() {
        return TABLE_NAME;
    }

    @Override
    protected String getColumnNameIdSQL() {
        return COLUMN_NAME_ID;
    }

    /**
     * Events to be stored.
     *
     * @param evt to be stored
     */
    public void save(AppEvent evt) {
        RecordEntity obj = new RecordEntity();

        obj.setTime(evt.getTimeCreated());
        obj.setUserName(evt.getUserId());
        obj.setTripId(evt.getTripId());
        obj.setProcessed(false);

        if (evt.getType() == EventType.EVENT_GPS_POSITION) {
            saveGPS(evt, obj);
        }

        if (evt.getType() == EventType.EVENT_OBD_PID) {
            saveOBD(evt, obj);
        }

        if (evt.getType() == EventType.EVENT_ACCEL) {
            saveACC(evt, obj);
        }

    }

    protected void saveACC(AppEvent evt, RecordEntity obj) {
        JSONObject jsonObj = new JSONObject();

        AccelerationEvent accelEvent = (AccelerationEvent) evt;

        obj.setType(RecordTypeEnum.TYPE_ACCEL.getValue());

        try {
            jsonObj.put(JsonTags.ACCELERATION_X, accelEvent.getX());
            jsonObj.put(JsonTags.ACCELERATION_Y, accelEvent.getY());
            jsonObj.put(JsonTags.ACCELERATION_Z, accelEvent.getZ());
        } catch (Exception e) {
            Log.d(AppLog.LOG_TAG_DB, "Exception while adding OBD event data to JSON object", e);
        }

        obj.setJson(jsonObj.toString());

        save(obj);
    }

    protected void saveGPS(AppEvent evt, RecordEntity obj) {
        JSONObject jsonObj = new JSONObject();

        obj.setType(RecordTypeEnum.TYPE_GPS.getValue());
        Location loc = ((GPSPositionEvent) evt).getLocation();
        if (loc == null) {
            return;
        }

        double m = 1000000.0;
        double k = 1000.0;
        try {
            jsonObj.put(JsonTags.GPS_LAT, m * loc.getLatitude());
            jsonObj.put(JsonTags.GPS_LNG, m * loc.getLongitude());
            jsonObj.put(JsonTags.GPS_ALT, loc.getAltitude());
            jsonObj.put(JsonTags.GPS_ACC, loc.getAccuracy());
            // TODO - Až bude přidaná rychlost odkomentovat
            //  jsonObj.put("speed", 3.6 * loc.getSpeed());
        } catch (Exception e) {
            Log.e(AppLog.LOG_TAG_DB, "Exception while adding GPS event data to JSON object", e);
        }

        obj.setJson(jsonObj.toString());

        ServiceManager.getInstance().db.incrementGpsDistance(loc);

        save(obj);
    }

    protected void saveOBD(AppEvent evt, RecordEntity obj) {
        OBDPidEvent obdEvent = (OBDPidEvent) evt;

        obj.setType(obdEvent.getMessage().getTag());

        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put(JsonTags.OTHER_VALUE, ((OBDPidEvent) evt).getValue());
        } catch (Exception e) {
            Log.e(AppLog.LOG_TAG_DB, "Exception while adding OBD event data to JSON object", e);
        }

        obj.setJson(jsonObj.toString());

        if (obdEvent.getMessage().getID() == ObdPidHelper.OBD_PID_ID_SPEED) {
            ServiceManager.getInstance().db.incrementObdDistance(obdEvent);
        }

        save(obj);
    }

}
