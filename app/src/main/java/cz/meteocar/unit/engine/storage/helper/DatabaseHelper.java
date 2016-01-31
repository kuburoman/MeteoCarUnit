package cz.meteocar.unit.engine.storage.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.FilterSettingEntity;

/**
 * Created by Toms, 2014.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, MySQLiteConfig.DATABASE_NAME, null, MySQLiteConfig.DATABASE_VERSION);
        AppLog.i(AppLog.LOG_TAG_DB, "TripDetails Constructor");
    }

    /**
     * Vytvoření databáze
     * - voláno automaticky
     *
     * @param db Zapisovatelná SqLite DB
     */
    public void onCreate(SQLiteDatabase db) {
        AppLog.i(AppLog.LOG_TAG_DB, "DB onCreate");
        db.execSQL(RecordHelper.SQL_CREATE_ENTRIES);
        db.execSQL(ObdPidHelper.SQL_CREATE_ENTRIES);
        db.execSQL(TripHelper.SQL_CREATE_ENTRIES);
        db.execSQL(UserHelper.SQL_CREATE_ENTRIES);
        db.execSQL(FilterSettingHelper.SQL_CREATE_ENTRIES);

        db.execSQL(ObdPidHelper.INSERT_INTO_ALL);
        db.execSQL(FilterSettingHelper.INSERT_INTO_ALL);
        db.execSQL(UserHelper.CREATE_DEFAULT_USER);
    }

    /**
     * Update databáze
     * - voláno automaticky (pokud nesedí aktuální verze DB s verzí v konfiuraci)
     *
     * @param db Zapisovatelná SqLite DB
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(RecordHelper.SQL_DELETE_ENTRIES);
        db.execSQL(ObdPidHelper.SQL_DELETE_ENTRIES);
        db.execSQL(TripHelper.SQL_DELETE_ENTRIES);
        db.execSQL(UserHelper.SQL_DELETE_ENTRIES);
        db.execSQL(FilterSettingHelper.SQL_DELETE_ENTRIES);

        onCreate(db);
    }

    /**
     * Update databáze na nižší verzy
     * - voláno automaticky (pokud nesedí aktuální verze DB s verzí v konfiuraci)
     *
     * @param db Zapisovatelná SqLite DB
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}