package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.helper.filter.ReducerType;
import cz.meteocar.unit.engine.storage.model.FilterSettingEntity;

/**
 * Servisa starajici se o vsechny zaznamy z jizdy
 */
public class FilterSettingHelper {

    /* Definice obsahu DB tabulky */
    public static final String TABLE_NAME = "filter_setting";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_OBD_CODE = "obd_code";
    public static final String COLUMN_NAME_ACTIVE = "active";
    public static final String COLUMN_NAME_REDUCE_TYPE = "reduce_type";
    public static final String COLUMN_NAME_REDUCE_VALUE = "reduce_value";
    public static final String COLUMN_NAME_ROUNDING = "rounding";
    public static final String COLUMN_NAME_ROUNDING_DECIMAL = "rounding_decimal";
    public static final String COLUMN_NAME_MAX_TIME = "max_time";
    public static final String COLUMN_NAME_UPDATE_TIME = "update_time";

    /* SQL statement pro vytvoreni tabulky */
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

    /* SQL statement pro smazani tabulky */
    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String SQL_GET_ALL = "SELECT  * FROM " + TABLE_NAME;

    /**
     * Nacte vsechny zaznamy
     *
     * @return ArrayList vsech objektu
     */
    public ArrayList<FilterSettingEntity> getAll() {

        //
        ArrayList<FilterSettingEntity> arr = new ArrayList<>();

        // pripravime kurzor k DB
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        // projdeme po radcich
        FilterSettingEntity obj;
        if (c.moveToFirst()) {
            while (c.isAfterLast() == false) {

                obj = new FilterSettingEntity();
                obj.setId(c.getInt(c.getColumnIndex(COLUMN_NAME_ID)));
                obj.setObdCode(c.getString(c.getColumnIndex(COLUMN_NAME_OBD_CODE)));
                obj.setReduceType(ReducerType.fromId(c.getInt(c.getColumnIndex(COLUMN_NAME_REDUCE_TYPE))));
                obj.setReduceValue(c.getDouble(c.getColumnIndex(COLUMN_NAME_REDUCE_VALUE)));
                obj.setRounding(c.getInt(c.getColumnIndex(COLUMN_NAME_ROUNDING)) != 0);
                obj.setRoundingDecimal(c.getInt(c.getColumnIndex(COLUMN_NAME_ROUNDING_DECIMAL)));
                obj.setMaxTime(c.getLong(c.getColumnIndex(COLUMN_NAME_MAX_TIME)));
                obj.setUpdateTime(c.getLong(c.getColumnIndex(COLUMN_NAME_UPDATE_TIME)));
                arr.add(obj);

                // dalsi
                c.moveToNext();
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
    public int save(FilterSettingEntity obj) {

        // nové values
        ContentValues values = new ContentValues();

        // nastavíme hodnoty
        values.put(COLUMN_NAME_OBD_CODE, obj.getObdCode());
        values.put(COLUMN_NAME_ACTIVE, obj.isActive());
        values.put(COLUMN_NAME_REDUCE_TYPE, obj.getReduceType().getId());
        values.put(COLUMN_NAME_REDUCE_VALUE, obj.getReduceValue());
        values.put(COLUMN_NAME_ROUNDING, obj.isRounding());
        values.put(COLUMN_NAME_ROUNDING_DECIMAL, obj.getRoundingDecimal());
        values.put(COLUMN_NAME_MAX_TIME, obj.getMaxTime());

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

    public FilterSettingEntity getById(int id) {

        SQLiteDatabase db = DB.helper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, "id = ?", new String[]{"" + id}, null, null, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            return getEntity(cursor);
        } else {
            return null;
        }
    }

    /**
     * Ziskani objektu z DB dle ID
     *
     * @param code Obd code zaznamu
     * @return {@Link FilterSettingEntity}
     */
    public FilterSettingEntity getByCode(String code) {

        SQLiteDatabase db = DB.helper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, "obd_code = ?", new String[]{code}, null, null, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            return getEntity(cursor);
        } else {
            return null;
        }
    }

    public void delete(int id) {
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        db.delete(TABLE_NAME, "id = ?", new String[]{"" + id});
    }

    protected FilterSettingEntity getEntity(Cursor cursor) {

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
