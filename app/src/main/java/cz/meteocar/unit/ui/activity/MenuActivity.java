package cz.meteocar.unit.ui.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import cz.meteocar.unit.R;
import cz.meteocar.unit.controller.MasterController;
import cz.meteocar.unit.controller.UserController;
import cz.meteocar.unit.ui.UIManager;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.gps.ServiceGPS;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.DB;

/**
 * TODO - Komentář ke třídě menu
 */
public class MenuActivity extends Activity {

    private DrawerLayout menuLayout;
    private ListView menuListView;
    private ActionBarDrawerToggle menuToggle;
    private ActionBar actionBar;
    private View actionBarView;
    private BitmapDrawable actionBarBg;

    //
    private TextView syncTextView;
    private String syncTextViewBaseText;

    /**
     * Handler nového "záměru" otevření aktivity, využito pro animaci
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    public static int instCount = 0;
    private int instID;

    /**
     * Vytvoření hlavní "menu" aktivity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);

        // zdvojení instance aktivity
        // TODO: opravit lepším způsobem
        instID = instCount++;

        // nastavíme instanci v manageru
        UIManager.getInstance().setMenuActivity(this);

        // nastaví FS
        //UIManager.getInstance().setupFullscreen(this);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN);    // flagy - nastaví typ okna FS
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        // zabráníme vypnutí a zhasnutí
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // nastavíme horizontální orientaci
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // opravuje problém s přetrvávající softw. klávesnicí
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        // nastaví View aktivity
        setContentView(R.layout.activity_main);

        // vytvoření rozvržení menu a listview pro umístění položek
        menuLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        menuListView = (ListView) findViewById(R.id.left_drawer);

        // stín menu vržený na aktuálně zobrazený fragment / obrazovku
        menuLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // adapter pro naplnění menu
        menuListView.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item,
                getResources().getStringArray(R.array.array_menu_item_names)) {

            /**
             * Override funkce pro zjištění velikosti datového pole, složitější adaptér neumí
             * zjistit velikost sám
             * @return Počet položek ke zobrazení
             */
            @Override
            public int getCount() {
                return getResources().getStringArray(R.array.array_menu_item_names).length + 1;
            }

            /**
             * Získání View které vykresluje konkrétní položku menu, pokud neexistuje je vytvořeno
             * @param position Pozice položky v datovém poli
             * @param convertView View které vykresluje konkrétní položku menu
             * @param parent Rodičovská skupina kam View patí
             * @return convertView
             */
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                // debug
                //AppLog.log(AppLog.LOG_MSG_INFO, "getView for position: "+position);

                // vytvoříme položku pokud pro danou pozici ještě není
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.drawer_list_item, null);
                    //AppLog.log(AppLog.LOG_MSG_INFO, "convertView created");
                }

                // první prvek nebudeme plnit
                if(position == 0){
                    convertView.setBackgroundResource(R.color.menu_background_first);
                    return  convertView;
                }

                // objekty položky
                ImageView imgIcon = (ImageView) convertView.findViewById(R.id.menu_item_icon);
                TextView txtTitle = (TextView) convertView.findViewById(R.id.menu_item_text);

                // naplnění obsahu (text a ikona)
                imgIcon.setImageResource(getResources().obtainTypedArray(
                        R.array.array_menu_item_icon).getResourceId(position-1, 0));
                txtTitle.setText(getResources().getStringArray(
                        R.array.array_menu_item_names)[position-1]);

                //
                if(position == UIManager.FRAGMENT_SYNC){
                    syncTextView = txtTitle;
                    syncTextViewBaseText = getResources().getStringArray(R.array.array_menu_item_names)[position-1];
                    updateSyncCountMenuItem();
                }

                //
                return convertView;
            }
        });

        // nastaví akci při označení položky menu
        menuListView.setOnItemClickListener(new ListView.OnItemClickListener(){
            /**
             * Handler kliknutí na položku
             * @param parent AdapterView
             * @param view View položky
             * @param position Pozice položky v menu
             * @param id ID položky v menu
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // oznámí manageru označení položky, pokud je vše v pořádku
                // manager odpoví True a můžeme zvolenou položku označit
                // a menu zavřít
                boolean isOK = UIManager.getInstance().onMenuItemSelected(position, getFragmentManager(), getWindow().getContext());

                // isOK použita, aby uzavření menu nemuselo čekat na zavedení nové
                // obrazovky
                if(isOK){
                    menuListView.setItemChecked(position, true);    // označení položky
                    menuLayout.closeDrawer(menuListView);           // uzavření menu
                }
            }
        });

        // nastaví klikací ikonu (vlevo nahoře)
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        //actionBar.setDisplayShowHomeEnabled(false);
        //actionBar.setDisplayUseLogoEnabled(false);
        actionBarBg = new BitmapDrawable(
                BitmapFactory.decodeResource(getResources(), R.drawable.app_action_bar_bg));
        actionBarBg.setTileModeX(Shader.TileMode.CLAMP);
        actionBarBg.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        actionBar.setBackgroundDrawable(actionBarBg);
        actionBarView = getWindow().getDecorView().findViewById(
                getResources().getIdentifier("action_bar_container", "id", "android"));
        actionBarView.setPadding(0, 0, 0, 0);
/*        View iconView = getWindow().getDecorView().findViewById(
                getResources().getIdentifier("home", "id", "android"));
        iconView.setPadding(0,0,0,0);
        iconView.setVisibility(View.GONE);*/


        // propojení navigační ikony a obrázku s menu
        menuToggle = new ActionBarDrawerToggle(this, menuLayout, R.drawable.ic_drawer,
                R.string.menu_open, R.string.menu_close) {

            boolean initBefore;
            boolean actionBarStatusBefore;
            int fragmentBefore;

            /**
             * Handler událostí menu
             * - použijeme pro zneplatnění a překreslení listu (update počtu objektů pro synchronizaci)
             *
             * {@link android.support.v4.widget.DrawerLayout.DrawerListener} callback method. If you do not use your
             * ActionBarDrawerToggle instance directly as your DrawerLayout's listener, you should call
             * through to this method from your own listener object.
             *
             * @param newState Nový stav zavíracího menu
             */
            @Override
            public void onDrawerStateChanged(int newState) {
                AppLog.i("Drawer state: "+newState);

                // pokud se menu odemklo, překreslíme
                if(newState == DrawerLayout.LOCK_MODE_LOCKED_OPEN){
                    invalidateOptionsMenu();
                }
                super.onDrawerStateChanged(newState);
            }

            /**
             * Handler otevření menu
             * @param view View celého menu
             */
            public void onDrawerOpened(View view) {
                invalidateOptionsMenu();

                // nastavíme stav action baru
                fragmentBefore = UIManager.getInstance().getActualFragment();
                actionBarStatusBefore = actionBar.isShowing();
                initBefore = true;

                // updatujeme počet položek k synchronizaci
                UIManager.getInstance().getMenuActivity().updateSyncCountMenuItem();

                //
                invalidateOptionsMenu();

                // schováme ho
                hideActionBar();
            }

            /**
             * Handler uzavření menu
             * @param view View celého menu
             */
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();

                // načteme předchozí stav action baru
                if(initBefore){

                    // otevřeme jej, pokud je to potřeba
                    if(actionBarStatusBefore){
                        if(fragmentBefore == UIManager.getInstance().getActualFragment()){
                            showActionBar();
                        }
                    }
                }
            }
        };
        menuLayout.setDrawerListener(menuToggle);

        // nastaví defaultní fragment / obrazovku
        //if (savedInstanceState == null) {
            UIManager.getInstance().onMenuItemSelected(UIManager.DEFAULT_FRAGMENT, getFragmentManager(), getApplicationContext());
        //}

        // připravíme dialogy
        initGPSDialog();
        initNoInternetDialog();

        //
        if(instID == 0){return;}

        // odložíme check hardware, aby stihli doběhnout animace (kt. by se jinak zasekli)
        (new Handler()).postDelayed(new Runnable(){
            public void run(){
                checkHardware();
            }}, 150);

        // odložíme spuštění videa
        (new Handler()).postDelayed(new Runnable(){
            public void run(){
                /*ServiceManager.getInstance().video.startInView(
                        (VideoView)findViewById(R.id.videoView),
                        UIManager.getInstance().getMenuActivity());*/
                ServiceManager.getInstance().video.updateVideoView(
                        (VideoView)findViewById(R.id.videoView),
                        UIManager.getInstance().getMenuActivity());
            }}, 300);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //zrušíme GPS dialog
        if(gpsDialog != null){
            if(gpsDialog.isShowing()){
                gpsDialog.dismiss();
            }
        }
        gpsDialogShowing = false;

        //
        //ServiceManager.getInstance().video.pause();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //
        //erviceManager.getInstance().video.continue();
    }


    /**
     * Otevře action bar
     */
    public void showActionBar(){
        try {
            actionBarView.getBackground().setAlpha(128);
        }catch(Exception e){}
    }

    /**
     * Schová action bar
     */
    private void hideActionBar(){
        try {
            //actionBarBg.setAlpha(0);
            actionBarView.getBackground().setAlpha(128);
        }catch(Exception e){}
    }

    /**
     * Připraví hlavní menu
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Handler událostí menu
     * - volá se při údálostech vysouvacího i normální menu (normální tu naštěstí nemáme)
     * @param item Označená položka
     * @return Vrací false pro volání dalších handlerů, true pokud se událost bude řešit pouze zde
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // jedná se o položku menu?
        // - pokus ano, nemusíme dále zpracovávat
        if(menuToggle.onOptionsItemSelected(item)){
            return true;
        }

        // super
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handler vytvoření nebo (zejména) obnovení View
     * Synchoronizuje Toggle objekt a aktuálním stavem menu
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(menuToggle!=null) {
            menuToggle.syncState();
        }
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    public void onBackPressed() {
        AppLog.i("BACK PRESSED");

        // máme už jen poslední záznam v zásobníku?
        // - to znamená prázný fragment, tj. nic ke zobrazení
        if(getFragmentManager().getBackStackEntryCount() <= 1){

            // zeptáme se trip controlleru, jestli můžeme zkončit
            if(MasterController.getInstance().trip.isActive()){
                requestTripExit(getWindow().getContext());
                return;
            }
        }

        super.onBackPressed();
    }

    /**
     * Zkontrolujeme stav hardwaru, případně vyzveme uživatele k jejich aktivaci
     */
    public void checkHardware(){
        askUserToActivateGPSifNeeded();
    }

    private boolean gpsDialogShowing = false;

    /**
     * Ověříme stav GPS a pokud má být zaplá a máme hardware, zeptáme se uživatele
     */
    public void askUserToActivateGPSifNeeded(){

        // není náhodou GPS zakázána?
        if(!DB.get().getBoolean(UserController.SETTINGS_KEY_GPS_ENABLED, false)){
            AppLog.i("GPS Check - DISABLED");
            return; }

        // pokud nemáme hardware
        if(ServiceManager.getInstance().gps.getStatus() == ServiceGPS.STATUS_NO_HARDWARE){
            AppLog.i("GPS Check - NO HARDWARE");
            return; }

        // GPS offline - tady se zeptáme uživatele
        /*if(ServiceManager.getInstance().gps.getStatus() == ServiceGPS.STATUS_GPS_OFFLINE){
            gpsDialog.show();
        }*/
        boolean gpsState = ServiceManager.getInstance().gps.isHardwareEnabled();
        //AppLog.i(AppLog.LOG_TAG_UI, "GPS State for dialog: "+gpsState);

        if(!gpsState && !gpsDialogShowing){
           //AppLog.i(AppLog.LOG_TAG_UI, "Will show GPS HW dialog");
           if(!gpsDialog.isShowing()) {
               gpsDialogShowing = true;
               gpsDialog.show();
           }
        }
    }

    private AlertDialog gpsDialog;

    /**
     * Připravíme dialog žádající uživatele o zapnutí GPS
     */
    private void initGPSDialog(){
        AppLog.i(null, "GPS Dialog Init");

        // připravíme si textview
        TextView txt = new TextView(this);
        txt.setText(Html.fromHtml(getResources().getString(R.string.dialog_gps_html)));
        int padding = getResources().getDimensionPixelOffset(R.dimen.fragment_padding);
        txt.setPadding(padding,padding,padding,0);
        //txt.setTextSize(padding);

        // uděláme builder, nastavíme text a titulek
        AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
        builder.setTitle(getResources().getString(R.string.dialog_gps_title));
        builder.setView(txt);

        gpsDialog = builder
                .setPositiveButton(R.string.dialog_gps_enable, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // TODO - dismiss by měl být jinde

                        // nabídneme nastavení
                        startActivity(new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                }).setNegativeButton(R.string.dialog_gps_disable, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        // změníme nastavení GPS
                        DB.set().putBoolean(UserController.SETTINGS_KEY_GPS_ENABLED, false).commit();
                        MasterController.getInstance().user.updateGPSstate();

                        // vyžádáme si odeslání GPS stavu
                        //ServiceManager.getInstance().gps.updateStatus();
                        //AppLog.i("GPS status after OFF: "+ServiceManager.getInstance().gps.getStatus());

                        // vyžádáme si restart
                        UIManager.getInstance().restartApp(MenuActivity.this);

                    }
                }).setCancelable(false).create();
    }

    public AlertDialog dialogNoInternet;

    /**
     * Připraví dialog a akce, pokud není internetové připojení
     */
    private void initNoInternetDialog(){

        // připravíme si textview
        TextView txt = new TextView(this);
        txt.setText(Html.fromHtml(getResources().getString(R.string.menu_net_offline)));
        int padding = getResources().getDimensionPixelOffset(R.dimen.fragment_padding);
        txt.setPadding(padding,padding,padding,0);
        //txt.setTextSize(padding);

        // uděláme builder, nastavíme text a titulek
        AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
        builder.setTitle(getResources().getString(R.string.login_net_offline_title));
        builder.setView(txt);

        // přidáme tlačítka, modalitu a sestavíme
        dialogNoInternet = builder
                .setPositiveButton( R.string.login_net_btn_wifi, null)
                .setNeutralButton(R.string.login_net_btn_mob, null)
                .setNegativeButton(R.string.login_net_btn_exit, null)
                .setCancelable(false).create();

        // přidáme akce tlačítek
        // - jedná se o nestandartní způsob, umožnující dialogu zůstat otevřený i po klinutí
        // - jinak by stačilo výše při přidávání před listenery místo null
        dialogNoInternet.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog){

                // Wifi
                dialogNoInternet.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        AppLog.i(null, "Zapni wifi!");
                        ServiceManager.getInstance().network.enableWifi();
                    }
                });


                // Mobilní internet
                dialogNoInternet.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        ServiceManager.getInstance().network.enableMobileNet();
                    }
                });

                // Ukončit
                dialogNoInternet.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        ServiceManager.getInstance().exitApp();
                    }
                });

            }
        });


    }

    /**
     * Vyžádá a provede restart aplikace
     */
    public void requestTripExit(final Context ctx){

        // vytvoříme a otevřeme dialog
        new AlertDialog.Builder(ctx)
                .setTitle(R.string.dialog_trip_exit_title)
                .setMessage(R.string.dialog_trip_exit_text)
                .setPositiveButton(R.string.dialog_trip_exit_storno, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setNeutralButton(R.string.dialog_trip_exit_off, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MasterController.getInstance().trip.stopTrip();
                ServiceManager.getInstance().exitApp();
                try {
                    finalize();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }).setNegativeButton(R.string.dialog_trip_exit_exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MasterController.getInstance().trip.stopTrip();
                dialog.dismiss();
            }
        }).create().show();
    }

    /**
     * Vyžádá a provede restart aplikace
     */
    public void requestAppRestart(final Context ctx){

        // vytvoříme a otevřeme dialog
        new AlertDialog.Builder(ctx)
            .setTitle(R.string.dialog_restart_title)
            .setMessage(R.string.dialog_restart_text)
            .setPositiveButton(R.string.dialog_restart_btn, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //dialog.dismiss();

                    // kontext a intent ID
                    Context context = ctx; //getApplicationContext();
                    int intentID = 10123;

                    // vytvoříme intent na spuštění vstupní aktivity (splash screen)
                    Intent splashActivity = new Intent(context, SplashActivity.class);
                    PendingIntent mPendingIntent = PendingIntent.getActivity(
                            context, intentID, splashActivity, PendingIntent.FLAG_CANCEL_CURRENT);

                    // ukončíme všechny služby
                    ServiceManager.getInstance().exitServices();

                    // nastavíme opětovné spuštění a ukončíme
                    AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                    System.exit(0);

                }
            }).setCancelable(false).create().show();
    }

    public void updateSyncCountMenuItem(){
        int numObjects = MasterController.getInstance().user.getNumberOfSyncObjects();
        AppLog.i(AppLog.LOG_TAG_UI, "Updating sync count: "+numObjects);
        syncTextView.setText(
                syncTextViewBaseText + "    "
                        + "[" + ((numObjects <= 0) ? "-" : numObjects) + "]"
        );
        syncTextView.postInvalidate();
    }
}