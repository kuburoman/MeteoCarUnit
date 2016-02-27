package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.UserEntity;

/**
 * Helper for saving and loading {@link UserEntity}.
 */
public class UserHelper extends AbstractHelper<UserEntity> {

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
        super(helper);
    }

    /**
     * Inserts new {@link UserEntity} into DB.
     *
     * @param obj {@link UserEntity}
     * @return Number of affected rows
     */
    @Override
    public int save(UserEntity obj) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_USERNAME, obj.getUsername());
        values.put(COLUMN_NAME_PASSWORD, obj.getPassword());
        values.put(COLUMN_NAME_LOGGED, obj.getLogged());

        return this.innerSave(obj.getId(), values);
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

    @Override
    protected UserEntity convert(Cursor cursor) {
        UserEntity obj = new UserEntity();
        obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
        obj.setUsername(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_USERNAME)));
        obj.setPassword(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PASSWORD)));
        obj.setLogged(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_LOGGED)) != 0);
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
