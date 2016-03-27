package cz.meteocar.unit.ui.activity;

import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

import java.util.Set;

import cz.meteocar.unit.R;
import cz.meteocar.unit.controller.MasterController;
import cz.meteocar.unit.controller.UserController;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.task.event.SyncWithServerChangedEvent;
import cz.meteocar.unit.ui.UIManager;
import cz.meteocar.unit.ui.activity.helpers.BoardUnitSettingActivityHelper;
import cz.meteocar.unit.ui.activity.helpers.CarSettingActivityHelper;
import cz.meteocar.unit.ui.activity.helpers.FilterSettingActivityHelper;
import cz.meteocar.unit.ui.activity.helpers.ObdPidSettingActivityHelper;

/**
 * Created by Toms, 2014.
 */
public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    // id políček nastavení - pozor, musí se shodovat s těmi v příslušném XML (my_settings.xml)
    public static final String SETTINGS_ID_OBD_ENABLED = "checkbox_obd_enabled";
    public static final String SETTINGS_ID_OBD_LIST = "button_obd_list";
    public static final String SETTINGS_ID_GPS_ENABLED = "checkbox_gps_enabled";
    public static final String SETTINGS_ID_GPS_LIST = "button_gps_list";
    public static final String SETTINGS_ID_OBD_PID_CAT = "button_obd_pids_category";
    public static final String NETWORK_ADDRESS = "network_address";
    public static final String FILTER_SETTINGS = "filter_settings";
    public static final String BOARD_UNIT_NAME = "board_unit_name";
    public static final String BOARD_UNIT_SECRET_KEY = "board_unit_secret_key";
    public static final String CHECKBOX_SYNC_WITH_SERVER = "checkbox_sync_with_server";
    public static final String BOARD_UNIT_SETTING_CAT = "board_unit_setting_category";
    public static final String CAR_SETTINGS = "car_settings";


    private ListPreference obdList;
    private CheckBoxPreference obdCheckBox;
    private CheckBoxPreference gpsCheckBox;
    private EditTextPreference networkEditText;

    private CheckBoxPreference syncWithServer;

    private FilterSettingActivityHelper filterDialog;
    private ObdPidSettingActivityHelper obdPidDialog;
    private BoardUnitSettingActivityHelper boardUnitDialog;
    private CarSettingActivityHelper carSettingDialog;

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        Log.e(AppLog.LOG_TAG_DEFAULT, "onContentChanged");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        UIManager.getInstance().showLoginActivity();
        Log.e(AppLog.LOG_TAG_DEFAULT, "onDestroy");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // načtene hierarchii z XML
        addPreferencesFromResource(R.xml.my_settings);

        // regsitrujeme on change listener
        this.getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        // najdeme list OBD zařízení a nastavíme popisek seznamu
        obdList = (ListPreference) findPreference(SETTINGS_ID_OBD_LIST);
        obdList.setSummary( // pokud máme nastavené OBD zř. vypíšeme jméno, jinak def. hlášku
                DB.get().getBoolean(UserController.SETTINGS_KEY_OBD_IS_SET, false) ?
                        DB.get().getString(UserController.SETTINGS_KEY_OBD_DEVICE_NAME,
                                getResources().getString(R.string.settings_obd_devices_none)) :
                        getResources().getString(R.string.settings_obd_devices_none)
        );

        // načte stav povolení OBD
        obdCheckBox = (CheckBoxPreference) findPreference(SETTINGS_ID_OBD_ENABLED);
        boolean obdEnabled = DB.get().getBoolean(
                UserController.SETTINGS_KEY_OBD_IS_ENABLED, false);
        obdCheckBox.setChecked(obdEnabled);

        networkEditText = (EditTextPreference) findPreference(NETWORK_ADDRESS);
        networkEditText.setText(DB.get().getString(NETWORK_ADDRESS, "http://meteocar.herokuapp.com"));

        syncWithServer = (CheckBoxPreference) findPreference(CHECKBOX_SYNC_WITH_SERVER);
        syncWithServer.setChecked(DB.getSyncWithServer());

        // načteme do listu obd zařízení data, pokud je OBD povoleno, jinak zakážeme
        if (obdEnabled) {
            setListPreferenceData(obdList);
            obdList.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    setListPreferenceData(obdList);
                    return false;
                }
            });
        } else {
            obdList.setEnabled(false);
        }

        // načte stav povolení GPS
        gpsCheckBox = (CheckBoxPreference) findPreference(SETTINGS_ID_GPS_ENABLED);
        gpsCheckBox.setChecked(
                DB.get().getBoolean(UserController.SETTINGS_KEY_GPS_ENABLED, false));

        obdPidDialog = new ObdPidSettingActivityHelper(this,
                getLayoutInflater().inflate(R.layout.obd_pid_editor, null), (PreferenceScreen) findPreference(SETTINGS_ID_OBD_PID_CAT));
        obdPidDialog.initDialog();
        obdPidDialog.createScreen();

        filterDialog = new FilterSettingActivityHelper(this,
                getLayoutInflater().inflate(R.layout.filter_setting, null), (PreferenceScreen) findPreference(FILTER_SETTINGS));
        filterDialog.initDialog();
        filterDialog.createScreen();

        boardUnitDialog = new BoardUnitSettingActivityHelper(this,
                getLayoutInflater().inflate(R.layout.board_unit_setting, null));
        boardUnitDialog.initDialog();

        carSettingDialog = new CarSettingActivityHelper(this,
                getLayoutInflater().inflate(R.layout.car_setting, null), (PreferenceScreen) findPreference(CAR_SETTINGS));
        carSettingDialog.initDialog();
        carSettingDialog.createScreen();

    }

    /**
     * Preference, která je nově otevírána
     */
    private boolean obdPidScreenOptionsInitialized = false;

    /**
     * Handler kliknutí na položku v celé hierarchii nastavení
     * - použijeme k vytvoření menu
     *
     * @param preferenceScreen Aktuální preference screen
     * @param preference       Preference na kterou bylo kliknuto
     * @return
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);

        // nemáme již inicializováno?
        if (obdPidScreenOptionsInitialized) {
            return false;
        }

        // menu budeme vytvářet jen pro obrazovku s PIDy
        if (preference == null) {
            return false;
        }
        if (preference.getKey() == null) {
            return false;
        }
        if (preference.getKey().equals(SETTINGS_ID_OBD_PID_CAT)) {
            obdPidDialog.treeClick((PreferenceScreen) preference);
            return false;
        }
        if (preference.getKey().equals(FILTER_SETTINGS)) {
            filterDialog.treeClick((PreferenceScreen) preference);
            return false;
        }
        if (preference.getKey().equals(BOARD_UNIT_SETTING_CAT)) {
            boardUnitDialog.showDialog();
            return false;
        }
        if (preference.getKey().equals(CAR_SETTINGS)) {
            carSettingDialog.treeClick((PreferenceScreen) preference);
            return false;
        }


        return false;
    }


    /**
     * Naplní list obd zařízení
     *
     * @param listPreference
     */
    protected void setListPreferenceData(ListPreference listPreference) {
        invalidateOptionsMenu();

        // získáme spárovaná BT zařízení
        Set<BluetoothDevice> devicesBT = ServiceManager.getInstance().obd.getBluetoothDevices();
        if (devicesBT == null) {
            AppLog.i(AppLog.LOG_TAG_OBD, "OBD devices NULL");
        }
        CharSequence[] entries = new CharSequence[devicesBT.size()];
        CharSequence[] entryValues = new CharSequence[devicesBT.size()];
        AppLog.i(AppLog.LOG_TAG_OBD, "OBD devices to list: " + devicesBT.size());

        // přečteme defaultní OBD zařízení, pokud jej máme
        boolean defaultEntryPresent = false;
        CharSequence defaultEntryValue = null;
        if (DB.get().getBoolean(UserController.SETTINGS_KEY_OBD_IS_SET, false)) {
            defaultEntryValue = DB.get().getString(UserController.SETTINGS_KEY_OBD_DEVICE_NAME, "");
        }

        // projdeme všechna zařízení, přidáme do polí a ověříme defaultní volbu
        int index = 0;
        for (BluetoothDevice device : devicesBT) {
            AppLog.i(AppLog.LOG_TAG_OBD, "device[" + index + "]: " + device.getName());
            AppLog.i(AppLog.LOG_TAG_OBD, "device[" + index + "]: " + device.getAddress());

            // připravíme položku
            entries[index] = device.getName();
            entryValues[index] = device.getName();
            //entryValues[index] = device.getAddress(); // pokud bychom chtěli používat BT adresy jako ID

            // ověříme zda není naším defaultním adaptérem
            if (defaultEntryValue != null) {
                if (defaultEntryValue.equals(entryValues[index])) {
                    defaultEntryPresent = true;
                }
            }

            //
            index++;
        }

        // nastavíme záznamy pro list
        listPreference.setEntries(entries);
        listPreference.setEntryValues(entryValues);

        // nastvíme defaultní zařízení, pokud jej máme v nastavení a pokud je zároven přítomno
        // mezi spárovanými zařízeními
        if (defaultEntryPresent) {
            listPreference.setDefaultValue(defaultEntryValue);
        }
    }

    /**
     * Handler změny nastavení
     *
     * @param sharedPreferences Objekt sdílených nastavení do kt. se uložila nová hodnota
     * @param key               Klíč pro právě změněné hodnoty
     */
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        AppLog.i(null, "Key changed: " + key);

        // povolení / zakázání OBD
        if (key.equals(SETTINGS_ID_OBD_ENABLED)) {
            boolean isEnabled = sharedPreferences.getBoolean(key, false);
            obdList.setEnabled(isEnabled);
            DB.set()
                    .putBoolean(UserController.SETTINGS_KEY_OBD_IS_ENABLED, isEnabled)
                    .commit();
            MasterController.getInstance().user.updateOBDstateAndRestart(
                    getWindow().getContext());
        }

        // nastavení bluetooth zařízení
        if (key.equals(SETTINGS_ID_OBD_LIST)) {
            String newDeviceName = sharedPreferences.getString(key, "");
            obdList.setSummary(newDeviceName);
            DB.set()
                    .putBoolean(UserController.SETTINGS_KEY_OBD_IS_SET, true)
                    .putString(UserController.SETTINGS_KEY_OBD_DEVICE_NAME, newDeviceName)
                    .commit();
            MasterController.getInstance().user.updateOBDstateAndRestart(
                    getWindow().getContext());
        }

        if (key.equals(CHECKBOX_SYNC_WITH_SERVER)) {
            boolean value = sharedPreferences.getBoolean(key, false);
            DB.setSyncWithServer(value);
            ServiceManager.getInstance().eventBus.post(new SyncWithServerChangedEvent()).asynchronously();
        }

        // povolení / zakázání GPS
        if (key.equals(SETTINGS_ID_GPS_ENABLED)) {
            boolean isEnabled = sharedPreferences.getBoolean(key, false);
            DB.set()
                    .putBoolean(UserController.SETTINGS_KEY_GPS_ENABLED, isEnabled)
                    .commit();
            MasterController.getInstance().user.updateGPSstateAndRestart(getWindow().getContext());
        }

        if (key.equals(NETWORK_ADDRESS)) {
            String value = sharedPreferences.getString(key, "http://meteocar.herokuapp.com");
            DB.setNetworkAddress(value);
        }
    }
}
