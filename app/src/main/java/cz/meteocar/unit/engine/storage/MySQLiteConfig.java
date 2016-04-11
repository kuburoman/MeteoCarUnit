package cz.meteocar.unit.engine.storage;

/**
 * My sql helper configuration.
 */
public interface MySQLiteConfig {

    String TYPE_ID = " INTEGER PRIMARY KEY";

    // date types
    String TYPE_TEXT = " TEXT";
    String TYPE_REAL = " REAL";
    String TYPE_INTEGER = " INTEGER";
    String TYPE_BOOLEAN = " BOOLEAN";

    String COMMA_SEP = ",";

    int DATABASE_VERSION = 15;
    String DATABASE_NAME = "AndroidCarTracker.db";


}
