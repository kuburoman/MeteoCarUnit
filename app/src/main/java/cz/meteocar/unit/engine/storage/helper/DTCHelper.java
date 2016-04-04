package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.obd.event.OBDPidEvent;
import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.DTCEntity;

/**
 * Database helper for DTC.
 */
public class DTCHelper extends AbstractHelper<DTCEntity> {

    protected static final String TABLE_NAME = "dtc_messages";
    protected static final String COLUMN_NAME_ID = "id";
    protected static final String COLUMN_NAME_TIME = "time";
    protected static final String COLUMN_NAME_TRIP_ID = "trip_id";
    protected static final String COLUMN_NAME_DTC_CODE = "dtc_code";
    protected static final String COLUMN_NAME_POSTED = "posted";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + MySQLiteConfig.TYPE_ID + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_TIME + MySQLiteConfig.TYPE_INTEGER + " DEFAULT 0" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_TRIP_ID + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_DTC_CODE + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_POSTED + MySQLiteConfig.TYPE_BOOLEAN + " DEFAULT ''" +
                    " )";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String SQL_GET_ALL = "SELECT  * FROM " + TABLE_NAME;

    public DTCHelper(DatabaseHelper helper) {
        super(helper);
    }

    @Override
    public int save(DTCEntity obj) {
        ContentValues values = newContentValues();
        values.put(COLUMN_NAME_TIME, obj.getTime());
        values.put(COLUMN_NAME_DTC_CODE, obj.getDtcCode());
        values.put(COLUMN_NAME_TRIP_ID, obj.getTripId());
        values.put(COLUMN_NAME_POSTED, obj.isPosted());
        try {
            return this.innerSave(obj.getId(), values);
        } catch (DatabaseException e) {
            Log.e(AppLog.LOG_TAG_DB, e.getMessage(), e);
            return -1;
        }
    }


    protected ContentValues newContentValues() {
        return new ContentValues();
    }

    /**
     * Sets records processed.
     *
     * @param id     List of id to process.
     * @param posted
     */
    public void updatePosted(List<Integer> id, Boolean posted) {
        String[] array = new String[id.size()];


        for (int i = 0; i < id.size(); i++) {
            array[i] = String.valueOf(id.get(i));
        }

        ContentValues values = newContentValues();
        values.put(COLUMN_NAME_POSTED, posted);

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

    public void save(OBDPidEvent obj) {
        List<String> dtcList = parseEveryFrame(obj.getRawResponse());

        for (String dtcMessage : dtcList) {
            DTCEntity dtc = new DTCEntity();
            dtc.setId(-1);
            dtc.setTripId(obj.getTripId());
            dtc.setDtcCode(dtcMessage);
            dtc.setTime(obj.getTimeCreated());
            dtc.setPosted(false);
            save(dtc);
        }
    }

    protected List<String> parseEveryFrame(String message) {
        message = message.replace(" ", "");
        if (message.length() % 14 != 0) {
            Log.d(AppLog.LOG_TAG_OBD, "DTC message corrupted");
            return new ArrayList<>();
        }

        List<String> results = new ArrayList<>();

        int first = 0;
        int last = 13;
        while (last <= message.length()) {
            String frame = message.substring(first, last);
            if (!frame.startsWith("43")) {
                Log.d(AppLog.LOG_TAG_OBD, "DTC Frame corrupted");
                return new ArrayList<>();
            }

            results.addAll(parseFrame(message.substring(first + 2, last + 1)));

            first += 14;
            last += 14;
        }
        return results;
    }

    protected List<String> parseFrame(String message) {
        int first = 0;
        int last = 3;
        List<String> frames = new ArrayList<>();
        while (last <= message.length()) {
            String single = parseSingleCode(message.substring(first, last + 1));
            if (!"P0000".equals(single)) {
                frames.add(single);
            }
            first += 4;
            last += 4;
        }
        return frames;
    }

    protected String parseSingleCode(String message) {
        String result = "";
        result += firstCharacter(message.substring(0, 1));
        result += secondCharacter(message.substring(0, 1));
        result += message.substring(1, 4);
        return result;
    }

    protected String firstCharacter(String character) {
        String binary = hexToBinary(character).substring(0, 2);
        switch (binary) {
            case "00":
                return "P";
            case "01":
                return "C";
            case "10":
                return "B";
            case "11":
                return "U";
            default:
                Log.d(AppLog.LOG_TAG_OBD, "First character didn't recognized.");
                return "";
        }
    }

    protected String secondCharacter(String character) {
        String binary = hexToBinary(character).substring(2, 4);
        return binaryToHex(binary);
    }

    protected String binaryToHex(String binary) {
        int decimal = Integer.parseInt(binary, 2);
        return Integer.toString(decimal, 16);
    }

    protected String hexToBinary(String hex) {
        int i = Integer.parseInt(hex, 16);
        String binString = Integer.toBinaryString(i);
        while (binString.length() < 4) {    //pad with 4 0's
            binString = "0" + binString;
        }
        return binString;
    }

    public int getNumberOfRecords(boolean posted) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_POSTED + " = ?", new String[]{!posted ? "0" : "1"}, null, null, null);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }


    public List<DTCEntity> getRecords(boolean posted, int maxRecords) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_POSTED + " = ?", new String[]{!posted ? "0" : "1"}, null, null, null, String.valueOf(maxRecords));

        return convertArray(cursor);
    }

    /**
     * Deletes all records that are posted and they not belong into active trip.
     *
     * @param tripId hash of active trip
     * @return True - success, False - failed to delete
     */
    public boolean delete(String tripId) {
        SQLiteDatabase db = helper.getReadableDatabase();

        return db.delete(getTableNameSQL(), COLUMN_NAME_TRIP_ID + " != ? and " + COLUMN_NAME_POSTED + " = ?", new String[]{tripId, String.valueOf(1)}) > 0;
    }

    @Override
    protected DTCEntity convert(Cursor cursor) {
        DTCEntity obj = new DTCEntity();
        obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
        obj.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_TIME)));
        obj.setTripId(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TRIP_ID)));
        obj.setDtcCode(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DTC_CODE)));
        obj.setPosted(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_POSTED)) != 0);
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
}
