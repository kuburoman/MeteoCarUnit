package cz.meteocar.unit.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.view.Window;
import android.view.WindowManager;

import cz.meteocar.unit.ui.UIManager;
import cz.meteocar.unit.ui.view.SplashView;
import cz.meteocar.unit.engine.log.AppLog;


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

    public static class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            AppLog.i(AppLog.LOG_TAG_NETWORK, "Receiving GCM intent (1)");
            AppLog.i(AppLog.LOG_TAG_NETWORK, "Extras: " + intent.getExtras().toString());

            String msg = intent.getExtras().getString("msg");
            AppLog.i(AppLog.LOG_TAG_NETWORK, "Msg: " + msg);

            if(!UIManager.getInstance().isInitialized()){
                AppLog.i("GCM trying to run the app");
                final Intent notificationIntent = new Intent(context, SplashActivity.class);
                notificationIntent.setAction(Intent.ACTION_MAIN);
                notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(notificationIntent);
            }
        }
    }
}
