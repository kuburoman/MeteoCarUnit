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
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
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

import cz.meteocar.unit.R;
import cz.meteocar.unit.controller.MasterController;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.ui.UIManager;

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

    /**
     * Handler nového "záměru" otevření aktivity, využito pro animaci
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    /**
     * Vytvoření hlavní "menu" aktivity
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);

        // nastavíme instanci v manageru
        UIManager.getInstance().setMenuActivity(this);

        // nastaví FS
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

                // vytvoříme položku pokud pro danou pozici ještě není
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.drawer_list_item, null);
                }

                // první prvek nebudeme plnit
                if (position == 0) {
                    convertView.setBackgroundResource(R.color.menu_background_first);
                    return convertView;
                }

                // objekty položky
                ImageView imgIcon = (ImageView) convertView.findViewById(R.id.menu_item_icon);
                TextView txtTitle = (TextView) convertView.findViewById(R.id.menu_item_text);

                // naplnění obsahu (text a ikona)
                imgIcon.setImageResource(getResources().obtainTypedArray(
                        R.array.array_menu_item_icon).getResourceId(position - 1, 0));
                txtTitle.setText(getResources().getStringArray(
                        R.array.array_menu_item_names)[position - 1]);

                return convertView;
            }
        });

        // nastaví akci při označení položky menu
        menuListView.setOnItemClickListener(new ListView.OnItemClickListener() {
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
                if (isOK) {
                    menuListView.setItemChecked(position, true);    // označení položky
                    menuLayout.closeDrawer(menuListView);           // uzavření menu
                }
            }
        });

        // nastaví klikací ikonu (vlevo nahoře)
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBarBg = new BitmapDrawable(
                BitmapFactory.decodeResource(getResources(), R.drawable.app_action_bar_bg));
        actionBarBg.setTileModeX(Shader.TileMode.CLAMP);
        actionBarBg.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        actionBar.setBackgroundDrawable(actionBarBg);
        actionBarView = getWindow().getDecorView().findViewById(
                getResources().getIdentifier("action_bar_container", "id", "android"));
        actionBarView.setPadding(0, 0, 0, 0);


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
                // pokud se menu odemklo, překreslíme
                if (newState == DrawerLayout.LOCK_MODE_LOCKED_OPEN) {
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
                if (initBefore && actionBarStatusBefore && fragmentBefore == UIManager.getInstance().getActualFragment()) {
                    showActionBar();
                }
            }
        };
        menuLayout.setDrawerListener(menuToggle);

        UIManager.getInstance().onMenuItemSelected(UIManager.DEFAULT_FRAGMENT, getFragmentManager(), getApplicationContext());
    }

    /**
     * Otevře action bar
     */
    public void showActionBar() {
        try {
            actionBarView.getBackground().setAlpha(128);
        } catch (Exception e) {
            Log.e(AppLog.LOG_TAG_DEFAULT, "Unable to show action bar", e);
        }
    }

    /**
     * Schová action bar
     */
    private void hideActionBar() {
        try {
            actionBarView.getBackground().setAlpha(128);
        } catch (Exception e) {
            Log.e(AppLog.LOG_TAG_DEFAULT, "Unable to hide action bar", e);
        }
    }

    /**
     * Prepares main menu
     *
     * @param menu to be prepared
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Handler of menu event
     *
     * @param item selected item in menu
     * @return True if it is item from menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (menuToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handler vytvoření nebo (zejména) obnovení View
     * Synchoronizuje Toggle objekt a aktuálním stavem menu
     *
     * @param savedInstanceState instance
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (menuToggle != null) {
            menuToggle.syncState();
        }
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    public void onBackPressed() {

        // máme už jen poslední záznam v zásobníku?
        // - to znamená prázný fragment, tj. nic ke zobrazení
        if (getFragmentManager().getBackStackEntryCount() <= 1 && MasterController.getInstance().trip.isActive()) {

            // zeptáme se trip controlleru, jestli můžeme zkončit
            requestTripExit(getWindow().getContext());
            return;
        }

        super.onBackPressed();
    }

    /**
     * Vyžádá a provede restart aplikace
     */
    public void requestTripExit(final Context ctx) {

        // vytvoříme a otevřeme dialog
        new AlertDialog.Builder(ctx)
                .setTitle(R.string.dialog_trip_exit_title)
                .setMessage(R.string.dialog_trip_exit_text)
                .setPositiveButton(R.string.dialog_trip_exit_storno, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
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
    public void requestAppRestart(final Context ctx) {

        // vytvoříme a otevřeme dialog
        new AlertDialog.Builder(ctx)
                .setTitle(R.string.dialog_restart_title)
                .setMessage(R.string.dialog_restart_text)
                .setPositiveButton(R.string.dialog_restart_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int intentID = 10123;

                        // vytvoříme intent na spuštění vstupní aktivity (splash screen)
                        Intent splashActivity = new Intent(ctx, SplashActivity.class);
                        PendingIntent mPendingIntent = PendingIntent.getActivity(
                                ctx, intentID, splashActivity, PendingIntent.FLAG_CANCEL_CURRENT);

                        // ukončíme všechny služby
                        ServiceManager.getInstance().exitServices();

                        // nastavíme opětovné spuštění a ukončíme
                        AlarmManager mgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        System.exit(0);

                    }
                }).setCancelable(false).create().show();
    }
}