package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.FilterSettingEntity;

/**
 * Helper for saving and loading {@link FilterSettingEntity}.
 */
public class FilterSettingHelper extends AbstractHelper<FilterSettingEntity> {

    private static final String TABLE_NAME = "filter_setting";
    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_ALGORITHM = "algorithm";
    private static final String COLUMN_NAME_TAG = "tag";
    private static final String COLUMN_NAME_VALUE = "value";
    private static final String COLUMN_NAME_ACTIVE = "active";
    private static final String COLUMN_NAME_UPDATE_TIME = "update_time";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + MySQLiteConfig.TYPE_ID + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_ALGORITHM + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_TAG + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_VALUE + MySQLiteConfig.TYPE_REAL + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_ACTIVE + MySQLiteConfig.TYPE_BOOLEAN + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_UPDATE_TIME + MySQLiteConfig.TYPE_INTEGER + " DEFAULT ''" +
                    " )";

    private static final String INSERT_INTO_START = "INSERT INTO " + TABLE_NAME + " (" +
            COLUMN_NAME_ID + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_ALGORITHM + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_TAG + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_VALUE + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_ACTIVE + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_UPDATE_TIME +
            " ) VALUES ";

    public static final String INSERT_INTO_ALL =
            "" +
                    INSERT_INTO_START +
                    "(1, 'RDP', 'obd_speed'      , '1.0', '1', '1451602800000')," +
                    "(2, 'RDP', 'obd_rpm'        , '1.0', '1', '1451602800000')," +
                    "(3, 'RDP', 'obd_throttle'   , '1.0', '1', '1451602800000')," +
                    "(4, 'RDP', 'obd_engine_temp', '1.0', '1', '1451602800000')," +
                    "(5, 'RDP', 'obf_airflow'    , '1.0', '1', '1451602800000');";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String SQL_GET_ALL = "SELECT  * FROM " + TABLE_NAME;

    public FilterSettingHelper(DatabaseHelper helper) {
        super(helper);
    }

    @Override
    public int save(FilterSettingEntity obj) throws DatabaseException {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_ALGORITHM, obj.getAlgorithm());
        values.put(COLUMN_NAME_TAG, obj.getTag());
        values.put(COLUMN_NAME_VALUE, obj.getValue());
        values.put(COLUMN_NAME_ACTIVE, obj.isActive());
        values.put(COLUMN_NAME_UPDATE_TIME, obj.getUpdateTime());
        return this.innerSave(obj.getId(), values);
    }

    /**
     * Returns {@link FilterSettingEntity} based on code.
     *
     * @param code OBD code of entity.
     * @return {@Link FilterSettingEntity}
     */
    public FilterSettingEntity getByCode(String code) {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_ALGORITHM + " = ?", new String[]{code}, null, null, null);

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

    @Override
    protected FilterSettingEntity convert(Cursor cursor) {
        FilterSettingEntity obj = new FilterSettingEntity();
        obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
        obj.setAlgorithm(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ALGORITHM)));
        obj.setTag(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TAG)));
        obj.setValue(cursor.getDouble(cursor.getColumnIndex(COLUMN_NAME_VALUE)));
        obj.setActive(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ACTIVE)) != 0);
        obj.setUpdateTime(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_UPDATE_TIME)));
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
