package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.CarSettingEntity;
import cz.meteocar.unit.engine.storage.model.FilterSettingEntity;

/**
 * Helper for saving and loading {@link FilterSettingEntity}.
 */
public class CarSettingHelper extends AbstractHelper<CarSettingEntity> {

    private static final String TABLE_NAME = "car_setting";
    private static final String COLUMN_NAME_CODE = "code";
    private static final String COLUMN_NAME_VALUE = "value";
    private static final String COLUMN_NAME_ACTIVE = "active";
    private static final String COLUMN_NAME_UPDATE_TIME = "update_time";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + MySQLiteConfig.TYPE_ID + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_CODE + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_VALUE + MySQLiteConfig.TYPE_REAL + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_ACTIVE + MySQLiteConfig.TYPE_BOOLEAN + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_UPDATE_TIME + MySQLiteConfig.TYPE_INTEGER + " DEFAULT ''" +
                    " )";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public CarSettingHelper(DatabaseHelper helper) {
        super(helper, TABLE_NAME);
    }

    @Override
    public int save(CarSettingEntity obj) throws DatabaseException {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_CODE, obj.getCode());
        values.put(COLUMN_NAME_VALUE, obj.getValue());
        values.put(COLUMN_NAME_ACTIVE, obj.isActive());
        values.put(COLUMN_NAME_UPDATE_TIME, obj.getUpdateTime());
        return this.innerSave(obj.getId(), values);
    }

    /**
     * Returns {@link CarSettingEntity} based on code.
     *
     * @param code code of entity.
     * @return {@link CarSettingEntity}
     */
    public CarSettingEntity getByCode(String code) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_CODE + " = ?", new String[]{code}, null, null, null);
        return convertSingle(cursor);
    }

    @Override
    protected CarSettingEntity convert(Cursor cursor) {
        CarSettingEntity obj = new CarSettingEntity();
        obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
        obj.setCode(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CODE)));
        obj.setValue(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_VALUE)));
        obj.setActive(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ACTIVE)) != 0);
        obj.setUpdateTime(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_UPDATE_TIME)));
        return obj;
    }

}
