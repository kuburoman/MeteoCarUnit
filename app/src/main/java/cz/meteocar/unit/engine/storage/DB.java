package cz.meteocar.unit.engine.storage;

import android.content.SharedPreferences;

import cz.meteocar.unit.engine.ServiceManager;

/**
 * Třída s duplicitními referencemi k objektům a metodám DB
 * - slouží pro pohodlnější přístup k často používaným DB objektům
 */
public class DB {

    // persistence key-value
    public static SharedPreferences get() {
        return ServiceManager.getInstance().db.getSettings();
    }

    public static SharedPreferences.Editor set() {
        return ServiceManager.getInstance().db.editSettings();
    }
}
