package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.TripEntity;

/**
 * Helper for saving and loading {@link TripHelper}.
 */
public class TripHelper extends AbstractHelper<TripEntity> {

    private static final String TABLE_NAME = "trip_details";
    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_JSON = "json";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + MySQLiteConfig.TYPE_ID + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_JSON + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" +
                    " )";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String SQL_GET_ALL = "SELECT  * FROM " + TABLE_NAME;

    public TripHelper(DatabaseHelper helper) {
        super(helper);
    }

    /**
     * Inserts new entities into database.
     *
     * @param obj {@link TripEntity}
     * @return id of entity
     */
    @Override
    public int save(TripEntity obj) throws DatabaseException {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_JSON, obj.getJson());

        return this.innerSave(obj.getId(), values);
    }

    /**
     * Return one trip from database.
     *
     * @return {@link TripEntity}
     */
    public TripEntity getOneTrip() {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, "1");

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
    protected TripEntity convert(Cursor cursor) {
        TripEntity obj = new TripEntity();
        obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
        obj.setJson(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_JSON)));
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
