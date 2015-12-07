package cz.meteocar.unit.ui.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

import cz.meteocar.unit.R;
import cz.meteocar.unit.controller.MasterController;
import cz.meteocar.unit.controller.UserController;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.model.ObdPidObject;

/**
 * Created by Toms, 2014.
 */
public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    // id políček nastavení - pozor, musí se shodovat s těmi v příslušném XML (my_settings.xml)
    public static final String SETTINGS_ID_OBD_ENABLED = "checkbox_obd_enabled";
    public static final String SETTINGS_ID_OBD_LIST = "button_obd_list";
    public static final String SETTINGS_ID_GPS_ENABLED = "checkbox_gps_enabled";
    public static final String SETTINGS_ID_GPS_LIST = "button_gps_list";
    public static final String SETTINGS_ID_OBD_PID_CAT = "button_obd_pids_category";

    private ListPreference obdList;
    private CheckBoxPreference obdCheckBox;
    private CheckBoxPreference gpsCheckBox;

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        AppLog.i("S changed");
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

        // načteme do listu obd zařízení data, pokud je OBD povoleno, jinak zakážeme
        if(obdEnabled) {
            setListPreferenceData(obdList);
            obdList.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    setListPreferenceData(obdList);
                    return false;
                }
            });
        }else{
            obdList.setEnabled(false);
        }

        // načte stav povolení GPS
        gpsCheckBox = (CheckBoxPreference) findPreference(SETTINGS_ID_GPS_ENABLED);
        gpsCheckBox.setChecked(
                DB.get().getBoolean(UserController.SETTINGS_KEY_GPS_ENABLED, false));

        // inicializujeme editovací dialog pro PIDy
        initObdPidDialog();

        // vytvoříme kategorii OBD PIDů
        createObdPIDScreen();
    }

    /**
     * Preference, která je nově otevírána
     */
    private boolean obdPidScreenOptionsInitialized = false;

    /**
     * Handler kliknutí na položku v celé hierarchii nastavení
     * - použijeme k vytvoření menu
     * @param preferenceScreen Aktuální preference screen
     * @param preference Preference na kterou bylo kliknuto
     * @return
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);

        //AppLog.i(AppLog.LOG_TAG_UI, "TreeClick preferenceScreen.getKey(): "+preferenceScreen.getKey());
        //AppLog.i(AppLog.LOG_TAG_UI, "TreeClick preference.getKey(): " + preference.getKey());
        //invalidateOptionsMenu();

        // nemáme již inicializováno?
        if(obdPidScreenOptionsInitialized){return false;}

        // menu budeme vytvářet jen pro obrazovku s PIDy
        if(preference == null){return false;}
        if(preference.getKey() == null){return false;}
        if(!preference.getKey().equals(SETTINGS_ID_OBD_PID_CAT)){return false;}

        // vycastujeme preference na screen (bezpečné - známe key)
        PreferenceScreen myScreen = (PreferenceScreen) preference;

        // připravíme si layout
        RelativeLayout layout = new RelativeLayout(this);
        layout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        // naplníme obsah
        Button btn = new Button(this, null, android.R.attr.buttonStyleSmall);
        btn.setText(getResources().getString(R.string.settings_obd_pids_add));
        btn.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        ((RelativeLayout.LayoutParams)btn.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ((RelativeLayout.LayoutParams)btn.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
        //((RelativeLayout.LayoutParams)btn.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_END);
        btn.setBackgroundColor(Color.TRANSPARENT);
        layout.addView(btn);
        TextView txt = new TextView(this);
        txt.setText(getResources().getString(R.string.settings_obd_pids_title));
        txt.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        txt.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        ((RelativeLayout.LayoutParams)txt.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        ((RelativeLayout.LayoutParams)txt.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
        int padding = getResources().getDimensionPixelOffset(R.dimen.fragment_padding);
        txt.setPadding(padding,padding*3/4,0,0);
        //((RelativeLayout.LayoutParams)txt.getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL);
        //((RelativeLayout.LayoutParams)txt.getLayoutParams()).addRule(RelativeLayout.CENTER_IN_PARENT);
        layout.addView(txt);

        // najdeme action bar a vložíme do něj nový layout
        Dialog dialog = myScreen.getDialog();

        //
        if(dialog == null){return false;}
        dialog.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        dialog.getActionBar().setCustomView(layout);

        // přidáme akci ke tlačíku
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddButtonClick();
            }
        });

        // ok
        obdPidScreenOptionsInitialized = true;
        return false;
    }

    /**
     * Handler kliknutí na tlačítko přidat u OBD PID obrazovky
     */
    private void onAddButtonClick(){
        AppLog.i(AppLog.LOG_TAG_UI, "Add button clicked");

        // přidáme PID, jen pokud se aktuálně nepřidává
        if(!obdPidDialog.isShowing()){
            int newID = ObdPidObject.addOneByCopying(1, "Nový PID");
            createObdPIDScreen();
        }
    }

    /**
     * Naplní list obd zařízení
     * @param listPreference
     */
    protected void setListPreferenceData(ListPreference listPreference) {
        invalidateOptionsMenu();

        // získáme spárovaná BT zařízení
        Set<BluetoothDevice> devicesBT = ServiceManager.getInstance().obd.getBluetoothDevices();
        if(devicesBT == null){AppLog.i(AppLog.LOG_TAG_OBD, "OBD devices NULL");}
        CharSequence[] entries = new CharSequence[devicesBT.size()];
        CharSequence[] entryValues = new CharSequence[devicesBT.size()];
        AppLog.i(AppLog.LOG_TAG_OBD, "OBD devices to list: "+devicesBT.size());

        // přečteme defaultní OBD zařízení, pokud jej máme
        boolean defaultEntryPresent = false;
        CharSequence defaultEntryValue = null;
        if(DB.get().getBoolean(UserController.SETTINGS_KEY_OBD_IS_SET, false)){
            defaultEntryValue = DB.get().getString(UserController.SETTINGS_KEY_OBD_DEVICE_NAME, "");
        }

        // projdeme všechna zařízení, přidáme do polí a ověříme defaultní volbu
        int index = 0;
        for (BluetoothDevice device : devicesBT) {
            AppLog.i(AppLog.LOG_TAG_OBD, "device["+index+"]: "+device.getName());
            AppLog.i(AppLog.LOG_TAG_OBD, "device["+index+"]: "+device.getAddress());

            // připravíme položku
            entries[index] = device.getName();
            entryValues[index] = device.getName();
            //entryValues[index] = device.getAddress(); // pokud bychom chtěli používat BT adresy jako ID

            // ověříme zda není naším defaultním adaptérem
            if(defaultEntryValue != null){
                if(defaultEntryValue.equals(entryValues[index])){
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
        if(defaultEntryPresent) {
            listPreference.setDefaultValue(defaultEntryValue);
        }
    }

    /**
     * Vytvořé obsah obrazovky nastavení OBD PIDů načtením z databáze
     */
    private void createObdPIDScreen(){

        // získáme a přemažeme kategorii
        PreferenceScreen cat = (PreferenceScreen)findPreference(SETTINGS_ID_OBD_PID_CAT);
        cat.removeAll();

        //create one check box for each setting you need
        //CheckBoxPreference checkBoxPreference = new CheckBoxPreference(this);
        //make sure each key is unique
        //checkBoxPreference.setKey("keyName");
        //checkBoxPreference.setChecked(true);
        //targetCategory.addPreference(checkBoxPreference);

        ArrayList<ObdPidObject> arr = ObdPidObject.getAll();
        int index = 0;
        for(ObdPidObject pid : arr){

            //
            AppLog.i(AppLog.LOG_TAG_UI, "Adding obd pid button: " + pid.getName());
            AppLog.i(AppLog.LOG_TAG_UI, "PID id: " + pid.getId());
            AppLog.i(AppLog.LOG_TAG_UI, "PID tag: " + pid.getTag());

            // vytvoříme tlačítko
            final int myID = pid.getId();
            Preference btn = new Preference(this);
            btn.setTitle(pid.getName());
            btn.setIcon(R.drawable.icon_tacho);
            btn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showObdDialog(myID);
                    return false;
                }
            });

            // přidáme do kategorie
            cat.addPreference(btn);
            index++;
        }
    }

    //
    AlertDialog obdPidDialog;
    View obdPidDialogView;
    int dialogDataID;

    /**
     * Zobrazí dialog k editaci OBD PIDu
     * @param id ID PIDu kt. editujeme
     */
    private void showObdDialog(int id){

        // id kt. editujeme
        dialogDataID = id;

        // změníme text
        //TextView txt = (TextView) obdPidDialogView.findViewById(R.id.dialog_obd_name_title);
        //if(txt == null){ AppLog.p("UI - Prefs OBD dialog editor view is NULL"); return; }
        //txt.setText("Showing menu item: " + i);

        // načteme z DB PID
        ObdPidObject pid = ObdPidObject.get(id);

        // pokud nebyl PID nalezen
        if(pid == null){
            TextView txt = (TextView) obdPidDialogView.findViewById(R.id.dialog_obd_name_title);
            if(txt == null){ AppLog.p("UI - Prefs OBD dialog editor view is NULL"); return; }
            txt.setText("PID not found, ID: " + id);
            obdPidDialog.show();
            return;
        }

        // jméno PIDu
        EditText editName = (EditText) obdPidDialogView.findViewById(R.id.dialog_obd_name_edit);
        if(editName!=null){editName.setText(pid.getName());}

        // JSON tag
        EditText editTag = (EditText) obdPidDialogView.findViewById(R.id.dialog_obd_tag_edit);
        if(editTag!=null){editTag.setText(pid.getTag());}
        if(pid.getLocked() == 1){
            editTag.setEnabled(false);
        }else{
            editTag.setEnabled(true);
        }

        // kód
        EditText editCode = (EditText) obdPidDialogView.findViewById(R.id.dialog_obd_code_edit);
        if(editCode!=null){editCode.setText(pid.getPidCode());}

        // vzorec
        EditText editFormula = (EditText) obdPidDialogView.findViewById(R.id.dialog_obd_formula_edit);
        if(editFormula!=null){editFormula.setText(pid.getFormula());}

        // min
        EditText editMin = (EditText) obdPidDialogView.findViewById(R.id.dialog_obd_min_edit);
        if(editMin != null){editMin.setText(""+pid.getMin());}

        // max
        EditText editMax = (EditText) obdPidDialogView.findViewById(R.id.dialog_obd_max_edit);
        if(editMax != null){editMax.setText(""+pid.getMax());}

        // aktivní? - podle toho zda je zamčený nebo aktivovaný
        CheckBox active = (CheckBox) obdPidDialogView.findViewById(R.id.dialog_obd_enabled);
        if(pid.getLocked() == 1){
            active.setChecked(true);
            active.setEnabled(true);
        }else{
            active.setEnabled(true);
            if(pid.getActive() == 1){
                active.setChecked(true);
            }else{
                active.setChecked(false);
            }
        }

        obdPidDialog.show();
    }

    /**
     * Připravíme dialog žádající uživatele o zapnutí GPS
     */
    private void initObdPidDialog(){

        // nacháme "nafouknout" view
        obdPidDialogView = getLayoutInflater().inflate(R.layout.obd_pid_editor, null);

        // uděláme builder, nastavíme text a titulek
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(getResources().getString(R.string.settings_obd_edit_window_title));
        builder.setView(obdPidDialogView);

        obdPidDialog = builder
            .setPositiveButton(R.string.settings_obd_edit_btn_cancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();


                }
            }).setNeutralButton(R.string.settings_obd_edit_btn_save, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        // připravíme si objekt
                        ObdPidObject obj = new ObdPidObject();

                        // id
                        obj.setId(dialogDataID);

                        // jméno PIDu
                        EditText editName = (EditText) obdPidDialogView.findViewById(R.id.dialog_obd_name_edit);
                        if(editName!=null){ obj.setName(editName.getText().toString()); }

                        // tag
                        EditText editTag = (EditText) obdPidDialogView.findViewById(R.id.dialog_obd_tag_edit);
                        if(editTag!=null){ obj.setTag(editTag.getText().toString()); }

                        // kód
                        EditText editCode = (EditText) obdPidDialogView.findViewById(R.id.dialog_obd_code_edit);
                        if(editCode!=null){ obj.setPidCode(editCode.getText().toString()); }

                        // vzorec
                        EditText editFormula = (EditText) obdPidDialogView.findViewById(R.id.dialog_obd_formula_edit);
                        if(editFormula!=null){ obj.setFormula(editFormula.getText().toString()); }

                        // min
                        EditText editMin = (EditText) obdPidDialogView.findViewById(R.id.dialog_obd_min_edit);
                        if(editMin!=null){ obj.setMin( Integer.parseInt(editMin.getText().toString())); }

                        // max
                        EditText editMax = (EditText) obdPidDialogView.findViewById(R.id.dialog_obd_max_edit);
                        if(editMax!=null){ obj.setMax(Integer.parseInt(editMax.getText().toString()));}

                        // active
                        CheckBox active = (CheckBox) obdPidDialogView.findViewById(R.id.dialog_obd_enabled);
                        if(active.isEnabled()){
                            obj.setActive(active.isChecked() ? 1 : 0);
                        }

                        // uložíme
                        if(ObdPidObject.save(obj) == 1){
                            dialog.dismiss();
                        } else {
                            AppLog.p(AppLog.LOG_TAG_DB, "Problem while saving OBD PID form data, incorrect save result");
                        }

                    }
                }).setNegativeButton(R.string.settings_obd_edit_btn_delete, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        // locked - nebudeme mazat
                        if( ObdPidObject.get(dialogDataID).getLocked() == 1 ){
                            return;
                        }

                        // vymažeme
                        AppLog.i(AppLog.LOG_TAG_DB, "Deleting PID");
                        ObdPidObject.delete(dialogDataID);
                        createObdPIDScreen();


                    }
                }).setCancelable(true).create();
    }

    /**
     * Handler změny nastavení
     * @param sharedPreferences Objekt sdílených nastavení do kt. se uložila nová hodnota
     * @param key Klíč pro právě změněné hodnoty
     */
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
        AppLog.i(null, "Key changed: " + key);

        // povolení / zakázání OBD
        if(key.equals(SETTINGS_ID_OBD_ENABLED)){
            boolean isEnabled = sharedPreferences.getBoolean(key, false);
            obdList.setEnabled(isEnabled);
            DB.set()
                    .putBoolean(UserController.SETTINGS_KEY_OBD_IS_ENABLED, isEnabled)
                    .commit();
            MasterController.getInstance().user.updateOBDstateAndRestart(
                    getWindow().getContext());
        }

        // nastavení bluetooth zařízení
        if(key.equals(SETTINGS_ID_OBD_LIST)){
            String newDeviceName = sharedPreferences.getString(key,"");
            obdList.setSummary(newDeviceName);
            DB.set()
                    .putBoolean(UserController.SETTINGS_KEY_OBD_IS_SET, true)
                    .putString(UserController.SETTINGS_KEY_OBD_DEVICE_NAME, newDeviceName)
                    .commit();
            MasterController.getInstance().user.updateOBDstateAndRestart(
                    getWindow().getContext());
        }

        // povolení / zakázání GPS
        if(key.equals(SETTINGS_ID_GPS_ENABLED)){
            boolean isEnabled = sharedPreferences.getBoolean(key,false);
            DB.set()
                    .putBoolean(UserController.SETTINGS_KEY_GPS_ENABLED, isEnabled)
                    .commit();
            MasterController.getInstance().user.updateGPSstateAndRestart(getWindow().getContext());
        }
    }
}
