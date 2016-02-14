package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.ObdPidEntity;

/**
 * Helper for saving and loading {@link ObdPidEntity}.
 */
public class ObdPidHelper {

    public static final int OBD_PID_ID_SPEED = 1;
    public static final int OBD_PID_ID_RPM = 2;
    public static final int OBD_PID_ID_THROTTLE = 3;
    public static final int OBD_PID_ID_ENGINE_TEMP = 4;
    public static final int OBD_PID_ID_MASS_AIRFLOW = 5;

    private static final String TABLE_NAME = "obd_pids";
    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_NAME = "name";
    private static final String COLUMN_NAME_TAG = "tag";
    private static final String COLUMN_NAME_PID_CODE = "pid_code";
    private static final String COLUMN_NAME_FORMULA = "formula";
    private static final String COLUMN_NAME_MIN = "min";
    private static final String COLUMN_NAME_MAX = "max";
    private static final String COLUMN_NAME_ACTIVE = "active";
    private static final String COLUMN_NAME_LOCKED = "locked";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + MySQLiteConfig.TYPE_ID + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_NAME + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_TAG + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_PID_CODE + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_FORMULA + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_MIN + MySQLiteConfig.TYPE_INTEGER + " DEFAULT 0" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_MAX + MySQLiteConfig.TYPE_INTEGER + " DEFAULT 0" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_ACTIVE + MySQLiteConfig.TYPE_INTEGER + " DEFAULT 0" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_LOCKED + MySQLiteConfig.TYPE_INTEGER + " DEFAULT 0" +
                    " )";

    private static final String INSERT_INTO_START = "INSERT INTO " + TABLE_NAME + " (" +
            COLUMN_NAME_ID + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_NAME + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_TAG + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_PID_CODE + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_FORMULA + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_MIN + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_MAX + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_ACTIVE + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_LOCKED +
            " ) VALUES ";

    public static final String INSERT_INTO_ALL =
            "" +
                    INSERT_INTO_START +
                    "(1, 'Rychlost'      , 'obd_speed'      , '010D1', 'A'              , 0  , 255  , 1, 1)," +
                    "(2, 'Otacky'        , 'obd_rpm'        , '010C2', '((A*256)+B)/4'  , 0  , 16384, 1, 1)," +
                    "(3, 'Pozice plynu'  , 'obd_throttle'   , '01111', '(A*100)/255'    , 0  , 100  , 1, 1)," +
                    "(4, 'Teplota motoru', 'obd_engine_temp', '01051', 'A-40'           , -40, 215  , 1, 1)," +
                    "(5, 'Aiflow'        , 'obf_airflow'    , '01102', '((A*256)+B)/100', 0  , 656  , 1, 1);";


    private static final String SQL_GET_ALL = "SELECT * FROM " + TABLE_NAME;

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;


    private DatabaseHelper helper;

    public ObdPidHelper(DatabaseHelper helper) {
        this.helper = helper;
    }

    /**
     * Return all entities in database.
     *
     * @return List of {@link ObdPidEntity}.
     */
    public List<ObdPidEntity> getAll() {
        return getAll(false);
    }

    /**
     * Return only entities that are active.
     *
     * @return List of {@link ObdPidEntity}
     */
    public ArrayList<ObdPidEntity> getAllActive() {
        return getAll(true);
    }

    protected ArrayList<ObdPidEntity> getAll(boolean onlyActive) {
        ArrayList<ObdPidEntity> arr = new ArrayList<>();

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor;
        if (!onlyActive) {
            cursor = db.rawQuery(SQL_GET_ALL, null);
        } else {
            cursor = db.rawQuery(SQL_GET_ALL + " WHERE " + COLUMN_NAME_ACTIVE + " = 1", null);
        }

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
     * Insert new entity into database
     *
     * @param obj {@link ObdPidEntity}
     * @return Number of affected rows.
     */
    public int save(ObdPidEntity obj) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_NAME, obj.getName());
        values.put(COLUMN_NAME_TAG, obj.getTag());
        values.put(COLUMN_NAME_PID_CODE, obj.getPidCode());
        values.put(COLUMN_NAME_FORMULA, obj.getFormula());
        values.put(COLUMN_NAME_MIN, obj.getMin());
        values.put(COLUMN_NAME_MAX, obj.getMax());
        values.put(COLUMN_NAME_LOCKED, obj.getLocked());
        values.put(COLUMN_NAME_ACTIVE, obj.getActive());

        SQLiteDatabase db = helper.getWritableDatabase();

        if (obj.getId() > 0) {
            values.put(COLUMN_NAME_ID, obj.getId());
            return (int) db.update(TABLE_NAME, values, COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(obj.getId())});
        } else {
            return (int) db.insert(TABLE_NAME, null, values);
        }
    }

    /**
     * Returns entity from database based on id.
     *
     * @param id of entity
     * @return {@link ObdPidEntity}
     */
    public ObdPidEntity get(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                return convert(cursor);
            } else {
                return null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Deletes entity base on id.
     *
     * @param id of entity
     * @return True - success, False - failed to delete
     */
    public boolean delete(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();

        return db.delete(TABLE_NAME, COLUMN_NAME_ID + " = " + id, null) > 0;
    }

    /**
     * Deletes all entities from databse.
     */
    public void deleteAll() {
        SQLiteDatabase db = helper.getReadableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    /**
     * Adds copy of entity based on id with new name.
     *
     * @param id      ID of copied entity
     * @param newName name of new entity
     * @return ID of new entity
     */
    public int addOneByCopying(int id, String newName) {
        ObdPidEntity obj = get(id);

        if (obj == null) {
            return -1;
        }

        obj.setId(-1);
        obj.setActive(0);
        obj.setLocked(0);

        return save(obj);
    }

    protected ObdPidEntity convert(Cursor cursor) {
        ObdPidEntity obj = new ObdPidEntity();
        obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
        obj.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NAME)));
        obj.setTag(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TAG)));
        obj.setPidCode(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PID_CODE)));
        obj.setFormula(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_FORMULA)));
        obj.setMin(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_MIN)));
        obj.setMax(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_MAX)));
        obj.setLocked(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_LOCKED)));
        obj.setActive(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ACTIVE)));
        return obj;
    }

}
