package cz.meteocar.unit.engine.storage;

import android.content.SharedPreferences;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.storage.helper.FilterSettingHelper;
import cz.meteocar.unit.engine.storage.helper.ObdPidHelper;
import cz.meteocar.unit.engine.storage.helper.RecordHelper;
import cz.meteocar.unit.engine.storage.helper.TripHelper;
import cz.meteocar.unit.engine.storage.helper.UserHelper;

/**
 * Třída s duplicitními referencemi k objektům a metodám DB
 * - slouží pro pohodlnější přístup k často používaným DB objektům
 */
public class DB {

    // DB helpery
    public static RecordHelper recordHelper;
    public static TripHelper tripHelper;
    public static UserHelper userHelper;
    public static ObdPidHelper obdPidHelper;
    public static FilterSettingHelper filterSettingHelper;

    // persistence key-value
    public static SharedPreferences get() {
        return ServiceManager.getInstance().db.getSettings();
    }

    public static SharedPreferences.Editor set() {
        return ServiceManager.getInstance().db.editSettings();
    }
}
