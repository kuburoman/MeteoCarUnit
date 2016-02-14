package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.helper.filter.ReducerType;
import cz.meteocar.unit.engine.storage.model.FilterSettingEntity;

/**
 * Helper for saving and loading {@link FilterSettingEntity}.
 */
public class FilterSettingHelper {

    private static final String TABLE_NAME = "filter_setting";
    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_OBD_CODE = "obd_code";
    private static final String COLUMN_NAME_ACTIVE = "active";
    private static final String COLUMN_NAME_REDUCE_TYPE = "reduce_type";
    private static final String COLUMN_NAME_REDUCE_VALUE = "reduce_value";
    private static final String COLUMN_NAME_ROUNDING = "rounding";
    private static final String COLUMN_NAME_ROUNDING_DECIMAL = "rounding_decimal";
    private static final String COLUMN_NAME_MAX_TIME = "max_time";
    private static final String COLUMN_NAME_UPDATE_TIME = "update_time";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + MySQLiteConfig.TYPE_ID + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_OBD_CODE + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_ACTIVE + MySQLiteConfig.TYPE_BOOLEAN + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_REDUCE_TYPE + MySQLiteConfig.TYPE_INTEGER + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_REDUCE_VALUE + MySQLiteConfig.TYPE_REAL + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_ROUNDING + MySQLiteConfig.TYPE_BOOLEAN + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_ROUNDING_DECIMAL + MySQLiteConfig.TYPE_INTEGER + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_MAX_TIME + MySQLiteConfig.TYPE_INTEGER + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_UPDATE_TIME + MySQLiteConfig.TYPE_INTEGER + " DEFAULT ''" +
                    " )";

    private static final String INSERT_INTO_START = "INSERT INTO " + TABLE_NAME + " (" +
            COLUMN_NAME_ID + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_OBD_CODE + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_ACTIVE + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_REDUCE_TYPE + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_REDUCE_VALUE + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_ROUNDING + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_ROUNDING_DECIMAL + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_MAX_TIME + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_UPDATE_TIME +
            " ) VALUES ";

    public static final String INSERT_INTO_ALL =
            "" +
                    INSERT_INTO_START +
                    "(1, 'obd_speed'      , '1', '1', '4.0', '1', '0', '60000', '1451602800000')," +
                    "(2, 'obd_rpm'        , '1', '1', '4.0', '1', '0', '60000', '1451602800000')," +
                    "(3, 'obd_throttle'   , '1', '1', '4.0', '1', '0', '60000', '1451602800000')," +
                    "(4, 'obd_engine_temp', '1', '1', '4.0', '1', '0', '60000', '1451602800000')," +
                    "(5, 'obf_airflow'    , '1', '1', '4.0', '1', '2', '60000', '1451602800000');";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String SQL_GET_ALL = "SELECT  * FROM " + TABLE_NAME;

    private DatabaseHelper helper;

    public FilterSettingHelper(DatabaseHelper helper) {
        this.helper = helper;
    }

    /**
     * Returns all {@link FilterSettingEntity} in database.
     *
     * @return List of {@link FilterSettingEntity}
     */
    public ArrayList<FilterSettingEntity> getAll() {
        ArrayList<FilterSettingEntity> arr = new ArrayList<>();

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_GET_ALL, null);

        try {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    arr.add(convert(cursor));
                    cursor.moveToNext();
                }
            }
            return arr;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Inserts new {@link FilterSettingEntity} database.
     *
     * @param obj {@link FilterSettingEntity}
     * @return Number of affected rows.
     */
    public int save(FilterSettingEntity obj) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_OBD_CODE, obj.getObdCode());
        values.put(COLUMN_NAME_ACTIVE, obj.isActive());
        values.put(COLUMN_NAME_REDUCE_TYPE, obj.getReduceType().getId());
        values.put(COLUMN_NAME_REDUCE_VALUE, obj.getReduceValue());
        values.put(COLUMN_NAME_ROUNDING, obj.isRounding());
        values.put(COLUMN_NAME_ROUNDING_DECIMAL, obj.getRoundingDecimal());
        values.put(COLUMN_NAME_MAX_TIME, obj.getMaxTime());

        SQLiteDatabase db = helper.getWritableDatabase();

        if (obj.getId() > 0) {
            values.put(COLUMN_NAME_ID, obj.getId());
            return (int) db.update(TABLE_NAME, values, COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(obj.getId())});
        } else {
            return (int) db.insert(TABLE_NAME, null, values);
        }
    }

    /**
     * Returns {@link FilterSettingEntity} based on id.
     *
     * @param id of entity
     * @return {@link FilterSettingEntity}
     */
    public FilterSettingEntity getById(int id) {
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
     * Returns {@link FilterSettingEntity} based on code.
     *
     * @param code OBD code of entity.
     * @return {@Link FilterSettingEntity}
     */
    public FilterSettingEntity getByCode(String code) {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_OBD_CODE + " = ?", new String[]{code}, null, null, null);

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
     * Deletes entity form database.
     *
     * @param id of entity
     */
    public void delete(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        db.delete(TABLE_NAME, COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(id)});
    }

    protected FilterSettingEntity convert(Cursor cursor) {
        FilterSettingEntity obj = new FilterSettingEntity();
        obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
        obj.setObdCode(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_OBD_CODE)));
        obj.setActive(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ACTIVE)) != 0);
        obj.setReduceType(ReducerType.fromId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_REDUCE_TYPE))));
        obj.setReduceValue(cursor.getDouble(cursor.getColumnIndex(COLUMN_NAME_REDUCE_VALUE)));
        obj.setRounding(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ROUNDING)) != 0);
        obj.setRoundingDecimal(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ROUNDING_DECIMAL)));
        obj.setMaxTime(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_MAX_TIME)));
        obj.setUpdateTime(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_UPDATE_TIME)));
        return obj;
    }

}
