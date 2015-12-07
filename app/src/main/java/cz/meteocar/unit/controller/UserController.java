package cz.meteocar.unit.controller;

import android.content.Context;

import net.engio.mbassy.listener.Handler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import cz.meteocar.unit.ui.UIManager;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.network.NetworkService;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.model.FileObject;
import cz.meteocar.unit.engine.storage.model.ObdPidObject;

/**
 * Kontroller představující uživatele
 * - stará se o stav přihlášení uživatele
 * - a o uživatelská nastavení (definuje klíče pro jejich persistenci)
 *
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

    // gcm
    /**
     * Je aplikace registrovaná pro GCM
     */
    public static final String SETTINGS_KEY_APP_GCM_REG = "key_app_gcm_reg";
    /**
     * Je GCM id uloženo také na serveru?
     */
    public static final String SETTINGS_KEY_APP_GCM_REG_SRV = "key_app_gcm_reg_srv";
    /**
     * GCM registarční ID, to potřebuje server aby mohl doručit zprávu přes GCM do zařízení
     */
    public static final String SETTINGS_KEY_APP_GCM_REG_ID = "key_app_gcm_reg_id";

    // ID k dotazům na server
    public static final String NETWORK_PIDS_RESPONSE = "network_get_pids";
    public static final String NETWORK_GCM_REG_RESPONSE = "gcm";
    public static final String NETWORK_GCM_REG_SRV_RESPONSE = "network_reg_srv_gcm";


    UserController(){

        // registrace na bus
        ServiceManager.getInstance().eventBus.subscribe(this);
    }

    /**
     * Inicializuje
     */
    public void init(){

        //
        //ServiceManager.getInstance().db.eraseSettings();

        // první spuštění?
        if(!ServiceManager.getInstance().db.getSettings().getBoolean("appInit", false)){

            // inicializujeme
            ServiceManager.getInstance().db.editSettings()

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

        // geistrujeme u GCM pokud je potřeba
        //checkGCMRegistration(); - nelze bez aktuálního kontextu

        // nastavíme uživatele v síťové službě
        ServiceManager.getInstance().network.setUser(getUserID(), getUploadKey());

        // updatujeme stav služeb podle uložených nastavení
        updateGPSstate();
        updateOBDstate();
    }

    // ---------- Gegistrace GCM -----------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    private Context context;

    /**
     * Zkontroluje, zda je o GCM registraci aplikace informován server
     */
    public void checkGCMRegistration(){

        // je již zaregistrováno
        if(DB.get().getBoolean(SETTINGS_KEY_APP_GCM_REG, false)){

            // pokud není o registraci informován server
            if(!DB.get().getBoolean(SETTINGS_KEY_APP_GCM_REG_SRV, false)){

                // informujeme server
                ServiceManager.getInstance().network.sendRequest(NETWORK_GCM_REG_RESPONSE,
                    "gcmRegisterApp.php", new HashMap<String, String>() {
                        HashMap<String, String> init() {
                            try{
                                put("model", new JSONObject()
                                    .put("id", getUserID())
                                    .put("key", getUploadKey())
                                    .put("gcm_id", DB.get().getString(SETTINGS_KEY_APP_GCM_REG_ID, ""))
                                    .toString()
                                );
                            }catch(Exception e){/* vždy se povede */}
                            return this;
                        }
                    }.init());
            }
        }
    }

    /**
     * Zpracování příchozí odpovědi na GCM registraci
     * - NETWORK_GCM_REG_RESPONSE: byla provedena GCM registrace
     * - NETWORK_GCM_REG_SRV_RESPONSE: server odpovídá na ohlášení registrace
     * @param evt Síťová událost - odpověď
     */
    @Handler
    public void handleNetworkGCMRegResponse(final NetworkService.NetworkRequestEvent evt) {

        // je to naše zpráva?
        if (evt.getID() == NETWORK_GCM_REG_SRV_RESPONSE) {

            // přečteme odpověď
            try{

                // je odpověď OK?
                if(evt.getResponse().getString("status").equals("ok")){

                    // nastavíme flag
                    DB.set().putBoolean(SETTINGS_KEY_APP_GCM_REG_SRV, true).commit();
                }else{

                    // došlo k chybě na serveru - ohlášení nebylo přijato
                }
            }catch(Exception e){
                AppLog.i(AppLog.LOG_TAG_NETWORK, "Response handler: C");
                e.printStackTrace();
            }

            return;
        }
    }

    /**
     * Anuluje existující GCM registraci
     */
    public void removeGCMregistration(){

        // je již zaregistrováno
        if(!DB.get().getBoolean(SETTINGS_KEY_APP_GCM_REG, false)){

            // smažeme pers. proměnné
            DB.set()
                    .putString(SETTINGS_KEY_APP_GCM_REG_ID, "")
                    .putBoolean(SETTINGS_KEY_APP_GCM_REG, false)
                    .commit();

        }
    }



    // ---------- Status OBD ---------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Updatujeme stav obd služby a vyždáme restart, je-li potřeba
     * @param ctx Kontext odkud vyšel požadavek
     */
    public void updateOBDstateAndRestart(Context ctx){
        if(updateOBDstate()){

            // pokud máme kontext vyžádáme restart
            if(ctx != null){
                UIManager.getInstance().restartApp(ctx);
            }
        }
    }

    /**
     * Updatuje stav OBD v závislosti na aktuálním nastavení
     * @return Je potřeba restart aplikace? True pokud ano, False pokud ne
     */
    public boolean updateOBDstate(){

        // potřebujeme restart aplikace?
        // - služba se totiž může jen zapnout a pak vypnout
        // - nelze ji ale zapnout po druhé poté co byla vypnuta, proto restart
        //boolean restartRequired = false;

        //obd - thread
        AppLog.i(AppLog.LOG_TAG_OBD,
                "UserController.SETTINGS_KEY_OBD_IS_ENABLED: "
                +DB.get().getBoolean(UserController.SETTINGS_KEY_OBD_IS_ENABLED, false));
        AppLog.i(AppLog.LOG_TAG_OBD,
                "obd.isRunning(): "+ServiceManager.getInstance().obd.isRunning());
        if( DB.get().getBoolean(UserController.SETTINGS_KEY_OBD_IS_ENABLED, false) !=
                ServiceManager.getInstance().obd.isRunning()){

            // stojí?
            if(!ServiceManager.getInstance().obd.isRunning()){
                AppLog.i(AppLog.LOG_TAG_OBD, "OBD not running");

                // stojí, neběžela už jednou?
                if(!ServiceManager.getInstance().obd.isFinalized()){
                    AppLog.i(AppLog.LOG_TAG_OBD, "OBD not finalized");

                    // ještě neběžela, máme vybrané zařízení?
                    // - pokud ne, nemůžeme nic dělat
                    if(DB.get().getBoolean(UserController.SETTINGS_KEY_OBD_IS_SET, false)){
                        AppLog.i(AppLog.LOG_TAG_OBD, "OBD device is SET");

                        // vše OK, spustíme s vybranným zařízením (default OBDII v př. chyby)
                        ServiceManager.getInstance().obd.setDeviceAndStart(
                            DB.get().getString(UserController.SETTINGS_KEY_OBD_DEVICE_NAME, "OBDII")
                        );
                    }else{

                        // vše ok, jen není vybrané žádné zařízení
                        AppLog.i(AppLog.LOG_TAG_OBD, "No OBD device set");

                        // tím pádem musíme zkontrolovat stav hardware a příp. povolit
                        // - jinak uživatel nebude moci v menu vybrat zařízení, ptž. nebude
                        //   fungovat vrácení seznamu spárovaných zařízení
                        ServiceManager.getInstance().obd.checkAndEnableBluetoothAsynchronously();
                    }
                }else{

                    // už běžela, na tuhle prácičku bude potřeba restart
                    //restartRequired = true;
                    AppLog.i(AppLog.LOG_TAG_OBD, "Restart required by OBD");
                    return true;
                }
            }else{

                // nestojí, zastavíme
                ServiceManager.getInstance().obd.exit();
            }
        }

        return false;
    }

    // ---------- Defaultní PIDy -----------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    public void checkDefaultPIDs(){

        // obd - nastavení
        // máme nastavené PIDy? pokud ne, nejprve je musíme načíst a až poté startovat OBD
        if(!DB.get().getBoolean(SETTINGS_KEY_OBD_PIDS_SET, false)){
            AppLog.i(AppLog.LOG_TAG_OBD, "OBD PIDs not set, requesting");

            // odešleme požadavek
            ServiceManager.getInstance().network.sendRequest(
                    NETWORK_PIDS_RESPONSE, "androidDownloadDefaultPids.php",
                    new HashMap<String, String>() {
                        HashMap<String, String> init() {
                            // zde nepotřebujeme JSON
                            return this;
                        }
                    }.init()
            );
        }else{
            AppLog.i(AppLog.LOG_TAG_OBD, "OBD PIDs OK");

            //
            UIManager.getInstance().showMenuActivity();
        }

    }

    /**
     * Zpracování příchozí odpovědi na JSON dotaz
     * @param evt
     */
    @Handler
    public void handleNetworkPidsResponse(final NetworkService.NetworkRequestEvent evt) {

        // je to naše zpráva?
        AppLog.i(AppLog.LOG_TAG_NETWORK, "Response commin: " + evt.getResponse().toString());
        if (evt.getID() != NETWORK_PIDS_RESPONSE) {
            return;
        }
        AppLog.i(AppLog.LOG_TAG_NETWORK, "Response is PIDs response");

        // přečteme odpověď
        boolean jsonError = false; // došlo k chybě při parsování?
        JSONArray containingArray;
        try{
            AppLog.i(AppLog.LOG_TAG_NETWORK, "Response handler: A");
            containingArray = evt.getResponse().getJSONArray("pids");

            // načteme pidy do DB
            ObdPidObject.insertFromJSONArray(containingArray.getJSONArray(0));
            ObdPidObject.insertFromJSONArray(containingArray.getJSONArray(1));
            ObdPidObject.insertFromJSONArray(containingArray.getJSONArray(2));
            ObdPidObject.insertFromJSONArray(containingArray.getJSONArray(3));
            ObdPidObject.insertFromJSONArray(containingArray.getJSONArray(4));

            // ok
            DB.set().putBoolean(SETTINGS_KEY_OBD_PIDS_SET, true).commit();

            // pokračujeme
            UIManager.getInstance().showMenuActivity();

            // ok, nyní ověříme stav služby a zapneme, pokud je potřeba
            //updateOBDstate();

            AppLog.i(AppLog.LOG_TAG_NETWORK, "Response handler: B");
        }catch(Exception e){
            AppLog.i(AppLog.LOG_TAG_NETWORK, "Response handler: C");
            e.printStackTrace();

            // chyba, vše smažeme
            ObdPidObject.deleteAll();
            jsonError = true;
            AppLog.i(AppLog.LOG_TAG_DB, "Error while saving pids!");

        }

        AppLog.i(AppLog.LOG_TAG_NETWORK, "Response handler exiting");
    }



    // ---------- Status GPS ---------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Updatujeme stav GPS služby a vyžádáme restart, je-li potřeba
     * @param ctx Kontext odkud vyšel požadavek
     */
    public void updateGPSstateAndRestart(Context ctx){
        if(updateGPSstate()){

            // pokud máme kontext vyžádáme restart
            if(ctx != null){
                UIManager.getInstance().restartApp(ctx);
            }
        }
    }

    /**
     * Updatuje stav GPS v závislosti na aktuálním nastavení
     * @return Je potřeba restart aplikace? True pokud ano, False pokud ne
     */
    public boolean updateGPSstate(){

        // je rozdíl mezi požadovaným a aktuálním stavem služby?
        if( DB.get().getBoolean(UserController.SETTINGS_KEY_GPS_ENABLED, false) !=
                ServiceManager.getInstance().gps.isRunning()){

            // stojí?
            if(!ServiceManager.getInstance().gps.isRunning()){

                // stojí, neběžela už jednou?
                if(!ServiceManager.getInstance().gps.isFinalized()){

                    // ještě neběžela, nastartujeme
                    ServiceManager.getInstance().gps.start();
                }else{

                    // už běžela, na tuhle prácičku bude potřeba restart
                    // restartRequired = true;
                    AppLog.i("Restart required by GPS");
                    return true;
                }
            }else{

                // nestojí, zastavíme
                ServiceManager.getInstance().gps.exit();
            }
        }

        return false;
    }

    // ---------- Data přihlášeného uživatele ----------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Přihlásí uživatele
     * - nastaví do persistence storu identifikační údaje potřebné pro komunikaci se serverem
     * @param id
     * @param name
     * @param email
     * @param key
     */
    public void logUser(int id, String name, String email, String key){
        DB.set()
                .putBoolean(SETTINGS_KEY_USER_LOGGED, true)
                .putInt(SETTINGS_KEY_USER_ID, id)
                .putString(SETTINGS_KEY_USER_NAME, name)
                .putString(SETTINGS_KEY_USER_EMAIL, email)
                .putString(SETTINGS_KEY_USER_UPLOAD_KEY, key)
                .commit();
    }

    /**
     * Vrátí ID přihlášeného uživatele
     */
    public int getUserID(){
        return DB.get().getInt(SETTINGS_KEY_USER_ID, -1);
    }

    /**
     * Vrátí uplaod key přihlášeného uživatele
     */
    public String getUploadKey(){
        return DB.get().getString(SETTINGS_KEY_USER_UPLOAD_KEY, null);
    }

    /**
     * Zjistí stav přihlášení uživatele
     * @return True pokud je uživatel přihlášený, False pokdu ne
     */
    public boolean isLogged(){

        // zjistíme z pers. DB
        AppLog.i(AppLog.LOG_TAG_UI, "User isLogged: "+DB.get().getBoolean(SETTINGS_KEY_USER_LOGGED, false));
        return DB.get().getBoolean(SETTINGS_KEY_USER_LOGGED, false);
    }

    /**
     * Odhlásí uživatele
     */
    public void logOutUser(){
        DB.set().putBoolean(SETTINGS_KEY_USER_LOGGED, false).commit();
    }

    // ---------- Položky k synchronizaci --------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Počet objektů k synchronizaci
     * @return
     */
    public int getNumberOfSyncObjects(){
        return FileObject.getNumberOfRecord();
    }
}
