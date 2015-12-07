package cz.meteocar.unit.engine.storage;

/**
 * Created by Toms, 2014.
 */
public class MySQLiteConfig {

    // id - pro sqlite používáme pouze jeden typ klíče
    public static final String TYPE_ID = " INTEGER PRIMARY KEY";

    // datové typy
    public static final String TYPE_TEXT = " TEXT";
    public static final String TYPE_REAL = " REAL";
    public static final String TYPE_INTEGER = " INTEGER";
    public static final String TYPE_AUTO_TIME = "  TIMESTAMP DEFAULT CURRENT_TIMESTAMP";

    // oddělovač
    public static final String COMMA_SEP = ",";

    // databáze
    public static final int DATABASE_VERSION = 8;
    public static final String DATABASE_NAME = "AndroidCarTracker.db";


}
