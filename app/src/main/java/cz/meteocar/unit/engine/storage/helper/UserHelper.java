package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.UserEntity;

/**
 * Helper for saving and loading {@link UserEntity}.
 */
public class UserHelper {

    private DatabaseHelper helper;

    private static final String TABLE_NAME = "meteocar_users";
    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_USERNAME = "username";
    private static final String COLUMN_NAME_PASSWORD = "password";
    private static final String COLUMN_NAME_LOGGED = "logged";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + MySQLiteConfig.TYPE_ID + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_USERNAME + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_PASSWORD + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_LOGGED + MySQLiteConfig.TYPE_BOOLEAN + " DEFAULT ''" +
                    " )";

    public static final String CREATE_DEFAULT_USER = "INSERT INTO " + TABLE_NAME + " (" +
            COLUMN_NAME_ID + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_USERNAME + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_PASSWORD + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_LOGGED + ") VALUES(" +
            "1, 'Johny', 'root', 0)";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String SQL_GET_ALL = "SELECT * FROM " + TABLE_NAME;

    /**
     * Constructor.
     *
     * @param helper {@Link DatabaseHelper}
     */
    public UserHelper(DatabaseHelper helper) {
        this.helper = helper;
    }

    /**
     * Returns all {@link UserEntity} stored in DB.
     *
     * @return List of {@link UserEntity}
     */
    public List<UserEntity> getAll() {
        ArrayList<UserEntity> arr = new ArrayList<>();

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
     * Inserts new {@link UserEntity} into DB.
     *
     * @param obj {@link UserEntity}
     * @return Number of affected rows
     */
    public UserEntity save(UserEntity obj) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_USERNAME, obj.getUsername());
        values.put(COLUMN_NAME_PASSWORD, obj.getPassword());
        values.put(COLUMN_NAME_LOGGED, obj.getLogged());

        SQLiteDatabase db = helper.getWritableDatabase();

        if (obj.getId() > 0) {
            values.put(COLUMN_NAME_ID, obj.getId());
            int id = db.update(TABLE_NAME, values, COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(obj.getId())});
            obj.setId(id);
            return obj;
        } else {
            int id = (int) db.insert(TABLE_NAME, null, values);
            obj.setId(id);
            return obj;
        }
    }

    /**
     * Set user logged.
     *
     * @param userEntity user to log
     */
    public void logUser(UserEntity userEntity) {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_LOGGED + " = ?", new String[]{"1"}, null, null, null);

        try {

            UserEntity obj;
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {

                    obj = new UserEntity();
                    obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
                    obj.setUsername(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_USERNAME)));
                    obj.setPassword(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PASSWORD)));
                    obj.setLogged(false);
                    save(obj);

                    cursor.moveToNext();
                }
            }

            userEntity.setLogged(true);
            save(userEntity);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    /**
     * Returns {@link UserEntity} based on id
     *
     * @param id of user entity
     * @return {@link UserEntity}
     */
    public UserEntity get(int id) {
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
     * Return user for given username and password
     *
     * @param username of user
     * @param password of user
     * @return {@link UserEntity}
     */
    public UserEntity getUser(String username, String password) {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_USERNAME + " = ? and " + COLUMN_NAME_PASSWORD + " = ?", new String[]{username, password}, null, null, null);

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
     * Return logged users
     *
     * @return {@link UserEntity}
     */
    public UserEntity getLoggedUser() {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_LOGGED + " = ?", new String[]{"1"}, null, null, null, "1");
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
     * Return number of records.
     *
     * @return number of records
     */
    public int getNumberOfRecord() {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.rawQuery(SQL_GET_ALL, null);
        int cnt = cursor.getCount();
        cursor.close();

        return cnt;
    }

    /**
     * Deletes all records in table
     */
    public void deleteAllRecords() {
        SQLiteDatabase db = helper.getReadableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    protected UserEntity convert(Cursor cursor) {
        UserEntity obj = new UserEntity();
        obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
        obj.setUsername(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_USERNAME)));
        obj.setPassword(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PASSWORD)));
        obj.setLogged(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_LOGGED)) != 0);
        return obj;

    }


}
