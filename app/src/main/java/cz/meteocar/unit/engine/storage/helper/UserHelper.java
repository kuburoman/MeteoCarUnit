package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.UserEntity;

/**
 * Helper for saving and loading {@link UserEntity}.
 */
public class UserHelper extends AbstractHelper<UserEntity> {

    private static final String TABLE_NAME = "meteocar_users";
    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_USERNAME = "username";
    private static final String COLUMN_NAME_PASSWORD = "pwn";
    private static final String COLUMN_NAME_ADMIN = "password";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + MySQLiteConfig.TYPE_ID + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_USERNAME + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_PASSWORD + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_ADMIN + MySQLiteConfig.TYPE_BOOLEAN + " DEFAULT ''" +
                    " )";

    public static final String CREATE_DEFAULT_USER = "INSERT INTO " + TABLE_NAME + " (" +
            COLUMN_NAME_ID + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_USERNAME + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_PASSWORD + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_ADMIN + ") VALUES(" +
            "1, 'Johny', 'root', 1)";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    /**
     * Constructor.
     *
     * @param helper {@Link DatabaseHelper}
     */
    public UserHelper(DatabaseHelper helper) {
        super(helper, TABLE_NAME);
    }

    /**
     * Inserts new {@link UserEntity} into DB.
     *
     * @param obj {@link UserEntity}
     * @return Number of affected rows
     */
    @Override
    public int save(UserEntity obj) throws DatabaseException {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_USERNAME, obj.getUsername());
        values.put(COLUMN_NAME_PASSWORD, obj.getPassword());
        values.put(COLUMN_NAME_ADMIN, obj.isAdmin());

        return this.innerSave(obj.getId(), values);
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

        return convertSingle(cursor);
    }

    /**
     * Return user for given username and password
     *
     * @param username of user
     * @return {@link UserEntity}
     */
    public UserEntity getUser(String username) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_USERNAME + " = ?", new String[]{username}, null, null, null);
        return convertSingle(cursor);
    }

    @Override
    protected UserEntity convert(Cursor cursor) {
        UserEntity obj = new UserEntity();
        obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
        obj.setUsername(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_USERNAME)));
        obj.setPassword(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PASSWORD)));
        obj.setAdmin(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ADMIN)) != 0);
        return obj;

    }
}
