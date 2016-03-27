package cz.meteocar.unit.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;

import cz.meteocar.unit.R;
import cz.meteocar.unit.controller.MasterController;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.ui.activity.LoginActivity;
import cz.meteocar.unit.ui.activity.MenuActivity;
import cz.meteocar.unit.ui.activity.SettingsActivity;
import cz.meteocar.unit.ui.activity.TripDetailActivity;
import cz.meteocar.unit.ui.fragments.DashboardFragment;

/**
 * Manager for UI.
 */
public class UIManager {

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
    private Context appContext;

    // activities
    private Activity splashScreen;
    private MenuActivity menuActivity;
    private Fragment dashboardFragment;

    // Menu items
    public static final int MENU_DASHBOARD = 1;
    public static final int MENU_TRIPS = 2;
    public static final int MENU_EXIT = 3;

    public static final int DEFAULT_FRAGMENT = MENU_DASHBOARD;

    private int actualFragment = DEFAULT_FRAGMENT;


    /**
     * Sets default page and context.
     *
     * @param splashAct Entry view of application
     */
    public void setSplashScreenAndInit(Activity splashAct) {

        splashScreen = splashAct;
        appContext = splashScreen.getApplicationContext();

        initFragments();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onSplashFinished();
            }
        }, SPLASH_TIMEOUT);

        ServiceManager.getInstance().init(splashScreen.getBaseContext());
    }

    /**
     * Action after welcome animation ended.
     * - initialise controllers
     */
    private void onSplashFinished() {

        MasterController.getInstance().init();

        if (DB.getLoggedUser() != null) {
            showMenuActivity();
        } else {
            showLoginActivity();
        }
    }

    /**
     * Sets instance of menu activity
     *
     * @param menuActivity currecnt menu activity
     */
    public void setMenuActivity(MenuActivity menuActivity) {
        this.menuActivity = menuActivity;
    }

    /**
     * Initialize fragments.
     */
    private void initFragments() {
        dashboardFragment = new DashboardFragment();
    }

    /**
     * Switch between login and menu activity.
     */
    public void showMenuActivity() {
        Intent intent = new Intent(appContext, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        // animation
        Bundle options = ActivityOptionsCompat.makeCustomAnimation(appContext, R.anim.fadein, R.anim.fadeout).toBundle();

        appContext.startActivity(intent, options);
        actualFragment = DEFAULT_FRAGMENT;
    }

    public void showTripsActivity() {

        Intent intent = new Intent(appContext, TripDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // animation
        Bundle options = ActivityOptionsCompat.makeCustomAnimation(appContext, R.anim.in, R.anim.out).toBundle();

        appContext.startActivity(intent, options);
    }

    /**
     * Shows settings activity.
     */
    public void showSettingsActivity() {
        Intent intent = new Intent(appContext, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Bundle options = ActivityOptionsCompat.makeCustomAnimation(appContext, R.anim.in, R.anim.out).toBundle();
        appContext.startActivity(intent, options);
    }

    /**
     * Shows login activity.
     */
    public void showLoginActivity() {

        Intent intent = new Intent(appContext, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Bundle options = ActivityOptionsCompat.makeCustomAnimation(appContext, R.anim.fadein, R.anim.fadeout).toBundle();
        appContext.startActivity(intent, options);
    }

    /**
     * Inform user about restart of application and does it.
     *
     * @param ctx that initialize application restart.
     */
    public void restartApp(Context ctx) {
        menuActivity.requestAppRestart(ctx);
    }


    /**
     * Sets full screen view.
     *
     * @param act Activity to switch intu full mode.
     */
    public void setupFullscreen(Activity act) {
        act.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        act.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * Handler for menu item clicked.
     *
     * @param position of item.
     * @return true if menu shloud be closed.
     */
    public boolean onMenuItemSelected(int position, FragmentManager fragmentManager, Context ctx) {
        if (position == MENU_TRIPS) {
            showTripsActivity();
            return true;
        }

        if (position == MENU_DASHBOARD) {

            fragmentManager
                    .beginTransaction()
                    .replace(R.id.content_frame, dashboardFragment)
                    .addToBackStack("fragment" + position)
                    .commit();

            actualFragment = position;
            return true;
        }

        if (position == MENU_EXIT) {
            if (MasterController.getInstance().trip.isActive()) {

                UIManager.getInstance().menuActivity.requestTripExit(ctx);
                return true;
            }

            DB.setLoggedUser(null);
            this.showLoginActivity();
            return true;
        }

        return true;
    }

    /**
     * Return id of active fragment.
     *
     * @return ID fragmentu
     */
    public int getActualFragment() {
        return actualFragment;
    }

    /**
     * Shows action bar in main activity with button menu.
     *
     * @param whichFragment request showing.
     */
    public void showActionBarFor(int whichFragment) {
        if (whichFragment > -1) {
            actualFragment = whichFragment;
        }

        menuActivity.showActionBar();
    }

    /**
     * Return Menu activity.
     *
     * @return {@link MenuActivity}.
     */
    public MenuActivity getMenuActivity() {
        return menuActivity;
    }

}
