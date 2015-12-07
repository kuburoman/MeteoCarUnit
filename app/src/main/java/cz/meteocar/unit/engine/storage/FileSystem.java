package cz.meteocar.unit.engine.storage;

import android.os.Environment;

/**
 * Created by Toms, 2014.
 */
public class FileSystem {
    public static final String APP_DIR = "Jezditocz";
    public static final String DIR_TRIP_LOGS = "trips";
    public static final String DIR_VIDEO = "videos";
    public static final String DIR_PHOTO = "photos";
    public static final String DIR_DEBUG = "debug";

    public static String getAppDir(){
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + APP_DIR;
    }

    public static String getTripLogDir(){
        return getAppDir() + "/" + DIR_TRIP_LOGS;
    }

    public static String getVideoDir(){
        return getAppDir() + "/" + DIR_VIDEO;
    }

    public static String getPhotoDir(){
        return getAppDir() + "/" + DIR_PHOTO;
    }
}
