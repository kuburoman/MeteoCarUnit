package cz.meteocar.unit.engine.storage.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;

/**
 * SQLiteOpenHelper for managing DB.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, MySQLiteConfig.DATABASE_NAME, null, MySQLiteConfig.DATABASE_VERSION);
        AppLog.i(AppLog.LOG_TAG_DB, "TripDetails Constructor");
    }

    /**
     * Creation of database.
     * Called automatically.
     *
     * @param db Writable database.
     */
    public void onCreate(SQLiteDatabase db) {
        AppLog.i(AppLog.LOG_TAG_DB, "DB onCreate");
        db.execSQL(RecordHelper.SQL_CREATE_ENTRIES);
        db.execSQL(ObdPidHelper.SQL_CREATE_ENTRIES);
        db.execSQL(TripHelper.SQL_CREATE_ENTRIES);
        db.execSQL(UserHelper.SQL_CREATE_ENTRIES);
        db.execSQL(FilterSettingHelper.SQL_CREATE_ENTRIES);
        db.execSQL(CarSettingHelper.SQL_CREATE_ENTRIES);
        db.execSQL(DTCHelper.SQL_CREATE_ENTRIES);

        db.execSQL(ObdPidHelper.INSERT_INTO_ALL);
        db.execSQL(FilterSettingHelper.INSERT_INTO_ALL);
        db.execSQL(UserHelper.CREATE_DEFAULT_USER);
    }

    /**
     * Update database.
     * - is called automatically (if version of database is not same)
     *
     * @param db Writable database
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(RecordHelper.SQL_DELETE_ENTRIES);
        db.execSQL(ObdPidHelper.SQL_DELETE_ENTRIES);
        db.execSQL(TripHelper.SQL_DELETE_ENTRIES);
        db.execSQL(UserHelper.SQL_DELETE_ENTRIES);
        db.execSQL(FilterSettingHelper.SQL_DELETE_ENTRIES);
        db.execSQL(CarSettingHelper.SQL_DELETE_ENTRIES);
        db.execSQL(DTCHelper.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void insertDefaultValues() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(RecordHelper.SQL_DELETE_ENTRIES);
        db.execSQL(ObdPidHelper.SQL_DELETE_ENTRIES);
        db.execSQL(TripHelper.SQL_DELETE_ENTRIES);
        db.execSQL(UserHelper.SQL_DELETE_ENTRIES);
        db.execSQL(FilterSettingHelper.SQL_DELETE_ENTRIES);
        db.execSQL(CarSettingHelper.SQL_DELETE_ENTRIES);
        db.execSQL(DTCHelper.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * Update database on lower version.
     * - is called automatically (if version of database is not same)
     *
     * @param db Writable database
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
