package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.TripEntity;

/**
 * Helper for saving and loading {@link TripHelper}.
 */
public class TripHelper {

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

    private final DatabaseHelper helper;

    public TripHelper(DatabaseHelper helper) {
        this.helper = helper;
    }

    /**
     * Return all entities in database.
     *
     * @return List of {@link TripEntity}
     */
    public List<TripEntity> getAll() {
        List<TripEntity> arr = new ArrayList<>();

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
     * Inserts new entities into database.
     *
     * @param obj {@link TripEntity}
     * @return id of entity
     */
    public int save(TripEntity obj) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_JSON, obj.getJson());

        SQLiteDatabase db = helper.getWritableDatabase();

        if (obj.getId() > 0) {

            values.put(COLUMN_NAME_ID, obj.getId());
            return (int) db.update(TABLE_NAME, values, COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(obj.getId())});
        } else {

            return (int) db.insert(TABLE_NAME, null, values);
        }
    }

    /**
     * Returns entity based on id.
     *
     * @param id of entity
     * @return {@link TripEntity} or null
     */
    public TripEntity get(int id) {
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

    /**
     * Return number of records in database.
     *
     * @return Number of rows.
     */
    public int getNumberOfRecord() {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.rawQuery(SQL_GET_ALL, null);
        int cnt = cursor.getCount();
        cursor.close();

        return cnt;
    }

    /**
     * Deletes all records from database.
     */
    public void deleteAllRecords() {
        SQLiteDatabase db = helper.getReadableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    /**
     * Deletes record from database based on id.
     *
     * @param id of entity
     */
    public void delete(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        db.delete(TABLE_NAME, COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(id)});
    }

    protected TripEntity convert(Cursor cursor) {
        TripEntity obj = new TripEntity();
        obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
        obj.setJson(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_JSON)));
        return obj;
    }
}
