package cz.meteocar.unit.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import cz.meteocar.unit.ui.UIManager;
import cz.meteocar.unit.ui.view.SplashView;


public class SplashActivity extends Activity {

    SplashView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE); // vypnutí hlavičky - vrchní řádku app
        UIManager.getInstance().setupFullscreen(this);

        // zapnutí obrazovky
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        // view
        view = new SplashView(this, UIManager.SPLASH_TIMEOUT);

        // nastavení úvodní activity do manageru
        UIManager.getInstance().setSplashScreenAndInit(this);

        //
        setContentView(view);
    }

    /**
     * Used here to empty memory.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        view.clearView();
    }
}
