package cz.meteocar.unit.engine.storage;

import android.content.SharedPreferences;

import cz.meteocar.unit.engine.ServiceManager;

/**
 * Třída s duplicitními referencemi k objektům a metodám DB
 * - slouží pro pohodlnější přístup k často používaným DB objektům
 */
public class DB {

    private static final String LOGGED_USER = "logged_user";
    private static final String LOGGED_USER_TIME = "LOGGED_USER_TIME";
    private static final String BOARD_UNIT_NAME = "board_unit_name";
    private static final String BOARD_UNIT_SECRET_KEY = "board_unit_secret_key";
    private static final String NETWORK_ADDRESS = "network_address";
    private static final String TRIP_ID = "trip_id";
    private static final String SYNC_SWITCH = "sync_switch";
    private static final String SHOW_FILTER_RESULT = "show_filter_result";


    // persistence key-value
    public static SharedPreferences get() {
        return ServiceManager.getInstance().db.getSettings();
    }

    public static SharedPreferences.Editor set() {
        return ServiceManager.getInstance().db.editSettings();
    }

    public static void setLoggedUser(String value) {
        set().putString(LOGGED_USER, value).commit();
    }

    public static String getLoggedUser() {
        return get().getString(LOGGED_USER, null);
    }

    public static void setLoggedUserTime(String value) {
        set().putString(LOGGED_USER_TIME, value).commit();
    }

    public static Long getLoggedUserTime() {
        return get().getLong(LOGGED_USER_TIME, 0L);
    }

    public static void setBoardUnitName(String value) {
        set().putString(BOARD_UNIT_NAME, value).commit();
    }

    public static String getBoardUnitName() {
        return get().getString(BOARD_UNIT_NAME, "root");
    }

    public static void setBoardUnitSecretKey(String value) {
        set().putString(BOARD_UNIT_SECRET_KEY, value).commit();
    }

    public static String getBoardUnitSecretKey() {
        return get().getString(BOARD_UNIT_SECRET_KEY, "root");
    }

    public static void setNetworkAddress(String value) {
        set().putString(NETWORK_ADDRESS, value).commit();
    }

    public static String getNetworkAddress() {
        return get().getString(NETWORK_ADDRESS, "http://meteocar.herokuapp.com");
    }

    public static void setTripId(String value) {
        set().putString(TRIP_ID, value).commit();
    }

    public static String getTripId() {
        return get().getString(TRIP_ID, "s");
    }

    public static void setSyncWithServer(boolean value) {
        set().putBoolean(SYNC_SWITCH, value).commit();
    }

    public static boolean getSyncWithServer() {
        return get().getBoolean(SYNC_SWITCH, false);
    }

    public static void setShowFilterResults(boolean value) {
        set().putBoolean(SHOW_FILTER_RESULT, value).commit();
    }

    public static boolean getShowFilterResults() {
        return get().getBoolean(SHOW_FILTER_RESULT, false);
    }
}
