package cz.meteocar.unit.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import cz.meteocar.unit.R;
import cz.meteocar.unit.controller.MasterController;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.ui.activity.LoginActivity;
import cz.meteocar.unit.ui.activity.MenuActivity;
import cz.meteocar.unit.ui.activity.SettingsActivity;
import cz.meteocar.unit.ui.activity.TripDetailActivity;
import cz.meteocar.unit.ui.fragments.DashboardFragment;
import cz.meteocar.unit.ui.fragments.DebugFragment;
import cz.meteocar.unit.ui.fragments.RecordsFragment;

/**
 * Created by Toms, 2014.
 */
public class UIManager {

    // verze
    public final String version = "1.9";

    // singleton pattern
    private static final UIManager INSTANCE = new UIManager();

    public static UIManager getInstance() {
        return INSTANCE;
    }

    /**
     * Celková doba zobrazení úvodní obrazovky
     */
    public static final int SPLASH_TIMEOUT = 1500;

    // kontext
    Context appContext;

    // aktivity
    private Activity splashScreen;
    private MenuActivity menuActivity;
    private SettingsActivity settingsActivity;

    // fragmenty - konstanty
    public static final int FRAGMENT_DASHBOARD = 1;
    public static final int FRAGMENT_RECORDS = 2;
    public static final int FRAGMENT_SYNC = 3;
    public static final int FRAGMENT_SETTINGS = 4;
    public static final int FRAGMENT_DEBUG = 5;
    public static final int FRAGMENT_TRIPS = 6;
    public static final int FRAGMENT_EXIT = 7;

    //
    public static final int DEFAULT_FRAGMENT = FRAGMENT_DASHBOARD;

    // fragmenty - proměnné
    private HashMap<Integer, Fragment> fragments = new HashMap();
    private int actualFragment = DEFAULT_FRAGMENT;

    /**
     * Byl UI manager již inicializován?
     * - pokud ano splashScreen již nebude null
     *
     * @return True - pokud ano, False - pokud ne
     */
    public boolean isInitialized() {
        return !(splashScreen == null);
    }

    /**
     * Nastaví úvodní obrazovku a kontext aplikace
     *
     * @param splashAct Úvodní obrazovka, vstupní bod aplikace
     */
    public void setSplashScreenAndInit(Activity splashAct) {

        // log
        AppLog.i("\\-----------------------------------------------/");
        AppLog.i(" AndroidCarTracker");
        AppLog.i(" Time: " + ((DateFormat) new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime()) + " (" + System.currentTimeMillis() + ")");
        AppLog.i(" UI version: " + UIManager.getInstance().version);
        AppLog.i(" Engine version: " + ServiceManager.getInstance().version);
        AppLog.i("/-----------------------------------------------\\");

        // nastavení úvodní aktivity a kontextu
        splashScreen = splashAct;
        appContext = splashScreen.getApplicationContext();

        // vytvoří aktivity
        //menuActivity = new MenuActivity();
        //settingsActivity = new SettingsActivity();

        // vytvoří fragmenty
        initFragments();

        // nastaví timeout pro úvodní obrazovku
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onSplashFinished();
            }
        }, SPLASH_TIMEOUT);

        // inicializuje manager služeb
        ServiceManager.getInstance().init(splashScreen.getBaseContext());
    }

    /**
     * Akce po zkončení úvodní animace
     * - provede inicializaci šlueb a kontrollerů
     */
    private void onSplashFinished() {

        // inicializuje manager služeb
        //ServiceManager.getInstance().init(splashScreen.getBaseContext());

        // inic. kontrolery (musí být inic. až po službách)
        MasterController.getInstance().init();

        // zkontrolujeme stav přihlášení uživatele
        if (MasterController.getInstance().user.isLogged()) {

            // je přihlášený, pokračujeme na dashboard
            showMenuActivity();
        } else {

            // nepřihlášen, přihlásíme
            showLoginActivity();
        }
    }

    /**
     * Naství instanci aktivity Menu
     *
     * @param menuActivity
     */
    public void setMenuActivity(MenuActivity menuActivity) {
        this.menuActivity = menuActivity;
    }

    /**
     * Instanciuje fragmenty do hashmapy
     */
    private void initFragments() {
        AppLog.i(AppLog.LOG_TAG_UI, "initFragments()");
        fragments.put(UIManager.FRAGMENT_DASHBOARD, new DashboardFragment());
        fragments.put(UIManager.FRAGMENT_RECORDS, new RecordsFragment());
        //fragments.put(UIManager.FRAGMENT_SETTINGS, new SettingsFragment());
        fragments.put(UIManager.FRAGMENT_DEBUG, new DebugFragment());
    }

    /**
     * Přepne zobrazení mezi úvodní obrazovkou a menu aktivitou
     */
    public void showMenuActivity() {

        //
        AppLog.i(null, "AAAAAAAAAAAAAAAAAAA");

        // vyvoláme aktivitu skrz intent
        // - new task a clear top tagy from smazání navigačního backstacku
        Intent intent = new Intent(appContext, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );

        // animace
        Bundle options = ActivityOptionsCompat.makeCustomAnimation(appContext, R.anim.fadein, R.anim.fadeout).toBundle();

        appContext.startActivity(intent, options);

        // animace
        //((Activity)appContext).overridePendingTransition(R.anim.in, R.anim.out);
        //splashScreen.finish();splashScreen.overridePendingTransition(R.anim.in, R.anim.out);

    }

    public void showTripsActivity() {

        // vyvoláme aktivitu skrz intent
        // - new task a clear top tagy from smazání navigačního backstacku
        Intent intent = new Intent(appContext, TripDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // animace
        Bundle options = ActivityOptionsCompat.makeCustomAnimation(appContext, R.anim.in, R.anim.out).toBundle();

        appContext.startActivity(intent, options);

        // animace
        //((Activity)appContext).overridePendingTransition(R.anim.in, R.anim.out);
        //splashScreen.finish();splashScreen.overridePendingTransition(R.anim.in, R.anim.out);

    }

    /**
     * Přepne zobrazení mezi úvodní obrazovkou a menu aktivitou
     */
    public void showSettingsActivity() {

        // vyvoláme aktivitu skrz intent
        // - new task a clear top tagy from smazání navigačního backstacku
        Intent intent = new Intent(appContext, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // animace
        Bundle options = ActivityOptionsCompat.makeCustomAnimation(appContext, R.anim.in, R.anim.out).toBundle();

        appContext.startActivity(intent, options);

        // animace
        //((Activity)appContext).overridePendingTransition(R.anim.in, R.anim.out);
        //splashScreen.finish();splashScreen.overridePendingTransition(R.anim.in, R.anim.out);

    }

    /**
     * Přepne zobrazení na přihlašovací obrazovku
     */
    public void showLoginActivity() {

        // nový intent (záměr) na spuštění aktivity
        Intent intent = new Intent(appContext, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        // animace
        Bundle options = ActivityOptionsCompat.makeCustomAnimation(appContext, R.anim.fadein, R.anim.fadeout).toBundle();

        // nastartujeme
        appContext.startActivity(intent, options);
    }

    /**
     * Informuje uživatele o restartu a provede jej
     *
     * @param ctx Kontext, který vyvolal restart (pro zobrazení dialogu)
     */
    public void restartApp(Context ctx) {
        menuActivity.requestAppRestart(ctx);
    }


    /**
     * Nastaví režim plné obrazovky (FS)
     *
     * @param act Aktivita, pro přepnutí do režimu FS
     */
    public void setupFullscreen(Activity act) {
        act.getWindow().setFlags(                               // flagy - nastaví typ okna FS
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        act.getWindow().getDecorView().setSystemUiVisibility(   // nastavení UI dekorací
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * Handler ozačení položky v menu
     *
     * @param position Pozice položky
     * @return True pokud se má zavřít menu, false pokud ne
     */
    public boolean onMenuItemSelected(int position, FragmentManager fragmentManager, Context ctx) {
        AppLog.log(AppLog.LOG_MSG_INFO, "Položka vybrána: " + position);

        // nastavení - vlastní aktivita
        if (position == FRAGMENT_SETTINGS) {
            showSettingsActivity();
            return true;
        }

        if (position == FRAGMENT_TRIPS) {
            showTripsActivity();
            return true;
        }

        // synchronizace - nemá UI
        if (position == FRAGMENT_SYNC) {
            MasterController.getInstance().trip.syncFilesToServer();
            return true;
        }

        // exit - není potřeba již nic měnit
        if (position == FRAGMENT_EXIT) {

            // máme aktivní trip?
            if (MasterController.getInstance().trip.isActive()) {

                // nelze ukončit, zobrazíme hlášku
                UIManager.getInstance().menuActivity.requestTripExit(ctx);
                return true;
            }

            // ukončíme aplikaci
            ServiceManager.getInstance().exitApp();
            return true;
        }

        // ověříme, zda vůbec máme předpřipravený vybraný fragment v hashmapě
        if (!fragments.containsKey(position)) {
            AppLog.log(AppLog.LOG_MSG_PROBLEM, "selected fragment not found in hashmap");
            try {
                displayToast(appContext.getResources().getString(R.string.menu_no_such_fragment));
            } catch (Exception e) {
                Log.e(AppLog.LOG_TAG_DEFAULT, "Error when displaying toast", e);
            }
            return false;   // nebudeme zavírat menu
        } else {
            AppLog.log(AppLog.LOG_MSG_PROBLEM, "fragment OK");
            //displayToast("Menu item selected: " + position);
        }

        // nový fragment
        Fragment newFragment = fragments.get(position);

        // transkace výměny fragmentů v menu aktivitě
        fragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, newFragment)
                .addToBackStack("fragment" + position)
                .commit();

        // označíme nový fragment jako aktuální
        actualFragment = position;

        // true - označit položku v menu a zavřít jej
        return true;
    }

    /**
     * Vrací ID aktuálního frgament / obrazovky
     *
     * @return ID fragmentu
     */
    public int getActualFragment() {
        return actualFragment;
    }

    /**
     * Zobrazí v hlavní aktivitě action bar (s tlačítkem "menu")
     * - tuto metodu volá fragment po zobrazení
     * - předává své id pro aktualizaci aktuálního fragmentu při použití tlačítka zpět
     * protože tím pádem nemá o přechodu UIManager žádnou jinou informaci
     *
     * @param whichFragment Fragment, který žádá o zobrazení (nastaví se jako aktuální)
     */
    public void showActionBarFor(int whichFragment) {

        // aktuální fragment
        if (whichFragment > -1) {
            actualFragment = whichFragment;
        }

        // otevřeme
        menuActivity.showActionBar();
    }

    /**
     * Zobrazení action baru
     */
    public void showActionBar() {
        showActionBarFor(-1);
    }


    /**
     * Vrátí hlavní "Menu" aktivitu
     * - zapouzdření
     *
     * @return menu aktivita
     */
    public MenuActivity getMenuActivity() {
        return menuActivity;
    }

    /**
     * Zobrazí na despleji krátkou zprávu
     *
     * @param txt Text zprávy
     */
    public void displayToast(String txt) {
        Toast.makeText(appContext, txt, Toast.LENGTH_LONG).show();
    }
}
