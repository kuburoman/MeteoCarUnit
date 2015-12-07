package cz.meteocar.unit.engine.log;

import android.util.Log;

/**
 * Created by Toms, 2014.
 */
public class AppLog {

    // singleton
    public static final AppLog appLog = new AppLog();
    public static AppLog getInstance() {
        return appLog;
    }

    // statické mentody pro logování
    public static void log(String msg){ AppLog.getInstance().logMessage(null, AppLog.LOG_MSG_INFO, msg); }
    public static void log(int code, String msg){ AppLog.getInstance().logMessage(null, code, msg); }
    public static void i(String msg){ AppLog.getInstance().logMessage(null, AppLog.LOG_MSG_INFO, msg); }
    public static void i(String tag, String msg){ AppLog.getInstance().logMessage(tag, AppLog.LOG_MSG_INFO, msg); }
    public static void p(String msg){ AppLog.getInstance().logMessage(null,AppLog.LOG_MSG_PROBLEM, msg); }
    public static void p(String tag, String msg){ AppLog.getInstance().logMessage(tag, AppLog.LOG_MSG_PROBLEM, msg); }
    public static void e(String msg){ AppLog.getInstance().logMessage(null,AppLog.LOG_MSG_ERROR, msg); }
    public static void e(String tag, String msg){ AppLog.getInstance().logMessage(tag, AppLog.LOG_MSG_ERROR, msg); }

    // kódy zpráv
    public static final int LOG_MSG_INFO = 0;
    public static final int LOG_MSG_PROBLEM = 1;
    public static final int LOG_MSG_ERROR = 2;
    public static final String[] codeText = new String[]{"info", "problem", "error"};

    // kódy tagů
    public static final String LOG_TAG_DEFAULT = "APP_DEFAULT";
    public static final String LOG_TAG_NETWORK = "NET";
    public static final String LOG_TAG_DB = "DB";
    public static final String LOG_TAG_OBD = "OBD";
    public static final String LOG_TAG_GPS = "GPS";
    public static final String LOG_TAG_UI = "UI";
    public static final String LOG_TAG_VIDEO = "VID";

    // nastavení
    public boolean useAndroidLog = true;

    // zpráva
    public void logMessage(String tag, int code, String msg){

        // defaultní tag
        if(tag == null){
            tag = LOG_TAG_DEFAULT;
        }

        // povolení / zakázání
        //if(tag.equals(LOG_TAG_GPS)){return;}
        //if(tag.equals(LOG_TAG_DB)){return;}

        // log do android konzole
        if(useAndroidLog){
            Log.i(tag, "[code:" + codeText[code] + "] " + msg);
        }
    }
}
