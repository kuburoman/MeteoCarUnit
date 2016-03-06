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

    // persistence key-value
    public static SharedPreferences get() {
        return ServiceManager.getInstance().db.getSettings();
    }

    public static SharedPreferences.Editor set() {
        return ServiceManager.getInstance().db.editSettings();
    }

    public static void setLoggedUser(String username) {
        set().putString(LOGGED_USER, username);
    }

    public static String getLoggedUser() {
        return get().getString(LOGGED_USER, null);
    }

    public static void setLoggedUserTime(String username) {
        set().putString(LOGGED_USER_TIME, username);
    }

    public static Long getLoggedUserTime() {
        return get().getLong(LOGGED_USER_TIME, 0L);
    }


}
