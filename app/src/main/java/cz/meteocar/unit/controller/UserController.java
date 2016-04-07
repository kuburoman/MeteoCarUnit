package cz.meteocar.unit.controller;

import android.content.Context;
import android.util.Log;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.model.UserEntity;
import cz.meteocar.unit.ui.UIManager;

/**
 * Kontroller představující uživatele
 * - stará se o stav přihlášení uživatele
 * - a o uživatelská nastavení (definuje klíče pro jejich persistenci)
 * <p/>
 * Created by Toms, 2014.
 */
public class UserController {

    // Klíče k persistentním nastavením
    // user
    public static final String SETTINGS_KEY_USER_LOGGED = "key_user_logged";
    public static final String SETTINGS_KEY_USER_ID = "key_user_id";
    public static final String SETTINGS_KEY_USER_NAME = "key_user_name";
    public static final String SETTINGS_KEY_USER_EMAIL = "key_user_email";
    public static final String SETTINGS_KEY_USER_UPLOAD_KEY = "key_user_upload_key";
    // obd bluetooth
    public static final String SETTINGS_KEY_OBD_IS_ENABLED = "key_obd_is_enabled";
    public static final String SETTINGS_KEY_OBD_IS_SET = "key_obd_is_set";
    public static final String SETTINGS_KEY_OBD_DEVICE_NAME = "user_key_obd_device_name";
    public static final String SETTINGS_KEY_OBD_DEVICE_ADDRESS = "user_key_obd_device_address";
    // obd pids
    public static final String SETTINGS_KEY_OBD_PIDS_SET = "user_key_obd_pids_set";
    // gps
    public static final String SETTINGS_KEY_GPS_ENABLED = "key_gps_enabled";

    // ID k dotazům na server
    public static final String NETWORK_PIDS_RESPONSE = "network_get_pids";


    UserController() {

        // registrace na bus
        ServiceManager.getInstance().eventBus.subscribe(this);
    }

    /**
     * Inicializuje
     */
    public void init() {

        // první spuštění?
        if (!ServiceManager.getInstance().getDB().getSettings().getBoolean("appInit", false)) {

            // inicializujeme
            ServiceManager.getInstance().getDB().editSettings()

                    // uživatel
                    .putBoolean("userLogged", false)

                            // nastavení OBD
                    .putBoolean(SETTINGS_KEY_OBD_IS_ENABLED, true)
                    .putBoolean(SETTINGS_KEY_OBD_IS_SET, false)
                    .putBoolean(SETTINGS_KEY_OBD_PIDS_SET, false)

                            // GPS
                    .putBoolean(SETTINGS_KEY_GPS_ENABLED, true)

                            //
                    .putBoolean("appInit", true)
                    .commit();

        }

        // updatujeme stav služeb podle uložených nastavení
        updateGPSstate();
        updateOBDstate();
    }

    // ---------- Status OBD ---------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Updatujeme stav obd služby a vyždáme restart, je-li potřeba
     *
     * @param ctx Kontext odkud vyšel požadavek
     */
    public void updateOBDstateAndRestart(Context ctx) {
        if (updateOBDstate() && ctx != null) {
            UIManager.getInstance().restartApp(ctx);
        }
    }

    /**
     * Updatuje stav OBD v závislosti na aktuálním nastavení
     *
     * @return Je potřeba restart aplikace? True pokud ano, False pokud ne
     */
    public boolean updateOBDstate() {

        // potřebujeme restart aplikace?
        // - služba se totiž může jen zapnout a pak vypnout
        // - nelze ji ale zapnout po druhé poté co byla vypnuta, proto restart

        //obd - thread
        Log.d(AppLog.LOG_TAG_OBD,
                "UserController.SETTINGS_KEY_OBD_IS_ENABLED: "
                        + DB.get().getBoolean(UserController.SETTINGS_KEY_OBD_IS_ENABLED, false));
        Log.d(AppLog.LOG_TAG_OBD,
                "obd.isRunning(): " + ServiceManager.getInstance().getOBD().isRunning());
        if (DB.get().getBoolean(UserController.SETTINGS_KEY_OBD_IS_ENABLED, false) !=
                ServiceManager.getInstance().getOBD().isRunning()) {

            // stojí?
            if (!ServiceManager.getInstance().getOBD().isRunning()) {
                Log.d(AppLog.LOG_TAG_OBD, "OBD not running");

                // stojí, neběžela už jednou?
                if (!ServiceManager.getInstance().getOBD().isFinalized()) {
                    Log.d(AppLog.LOG_TAG_OBD, "OBD not finalized");

                    // ještě neběžela, máme vybrané zařízení?
                    // - pokud ne, nemůžeme nic dělat
                    if (DB.get().getBoolean(UserController.SETTINGS_KEY_OBD_IS_SET, false)) {
                        Log.d(AppLog.LOG_TAG_OBD, "OBD device is SET");

                        // vše OK, spustíme s vybranným zařízením (default OBDII v př. chyby)
                        ServiceManager.getInstance().getOBD().setDeviceAndStart(
                                DB.get().getString(UserController.SETTINGS_KEY_OBD_DEVICE_NAME, "OBDII")
                        );
                    } else {

                        // vše ok, jen není vybrané žádné zařízení
                        Log.d(AppLog.LOG_TAG_OBD, "No OBD device set");

                        // tím pádem musíme zkontrolovat stav hardware a příp. povolit
                        // - jinak uživatel nebude moci v menu vybrat zařízení, ptž. nebude
                        //   fungovat vrácení seznamu spárovaných zařízení
                        ServiceManager.getInstance().getOBD().checkAndEnableBluetoothAsynchronously();
                    }
                } else {

                    // už běžela, na tuhle prácičku bude potřeba restart
                    Log.d(AppLog.LOG_TAG_OBD, "Restart required by OBD");
                    return true;
                }
            } else {

                // nestojí, zastavíme
                ServiceManager.getInstance().getOBD().exit();
            }
        }

        return false;
    }

    // ---------- Status GPS ---------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Updatujeme stav GPS služby a vyžádáme restart, je-li potřeba
     *
     * @param ctx Kontext odkud vyšel požadavek
     */
    public void updateGPSstateAndRestart(Context ctx) {
        if (updateGPSstate() && ctx != null) {
            UIManager.getInstance().restartApp(ctx);
        }
    }

    /**
     * Updatuje stav GPS v závislosti na aktuálním nastavení
     *
     * @return Je potřeba restart aplikace? True pokud ano, False pokud ne
     */
    public boolean updateGPSstate() {

        // je rozdíl mezi požadovaným a aktuálním stavem služby?
        if (DB.get().getBoolean(UserController.SETTINGS_KEY_GPS_ENABLED, false) !=
                ServiceManager.getInstance().getGPS().isRunning()) {

            // stojí?
            if (!ServiceManager.getInstance().getGPS().isRunning()) {

                // stojí, neběžela už jednou?
                if (!ServiceManager.getInstance().getGPS().isFinalized()) {

                    // ještě neběžela, nastartujeme
                    ServiceManager.getInstance().getGPS().start();
                } else {

                    // už běžela, na tuhle prácičku bude potřeba restart
                    Log.d(AppLog.LOG_TAG_GPS, "Restart required by GPS");
                    return true;
                }
            } else {

                // nestojí, zastavíme
                ServiceManager.getInstance().getGPS().exit();
            }
        }

        return false;
    }

    // ---------- Data přihlášeného uživatele ----------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * @param password Password of user
     */
    public void updateUser(String username, String password, boolean isAdmin) {
        UserEntity user = ServiceManager.getInstance().getDB().getUserHelper().getUser(username);
        if (user == null) {
            user = new UserEntity();
        }
        user.setUsername(username);
        user.setPassword(password);
        user.setAdmin(isAdmin);

        try {
            ServiceManager.getInstance().getDB().getUserHelper().save(user);
        } catch (DatabaseException e) {
            Log.e(AppLog.LOG_TAG_DB, e.getMessage(), e);
        }
    }

    public boolean isUserAdmin(String username) {
        UserEntity user = ServiceManager.getInstance().getDB().getUserHelper().getUser(username);
        if (user == null) {
            return false;
        }
        return user.isAdmin();
    }


    public boolean verifyUser(String username, String password) {
        UserEntity user = ServiceManager.getInstance().getDB().getUserHelper().getUser(username, password);
        return user != null;
    }

    /**
     * Vrátí ID přihlášeného uživatele
     */
    public int getUserID() {
        return DB.get().getInt(SETTINGS_KEY_USER_ID, -1);
    }

    /**
     * Vrátí uplaod key přihlášeného uživatele
     */
    public String getUploadKey() {
        return DB.get().getString(SETTINGS_KEY_USER_UPLOAD_KEY, null);
    }

    /**
     * Zjistí stav přihlášení uživatele
     *
     * @return True pokud je uživatel přihlášený, False pokdu ne
     */
    public boolean isLogged() {

        // zjistíme z pers. DB
        Log.d(AppLog.LOG_TAG_UI, "User isLogged: " + DB.get().getBoolean(SETTINGS_KEY_USER_LOGGED, false));
        return DB.get().getBoolean(SETTINGS_KEY_USER_LOGGED, false);
    }

    /**
     * Odhlásí uživatele
     */
    public void logOutUser() {
        DB.set().putBoolean(SETTINGS_KEY_USER_LOGGED, false).commit();
    }

}
