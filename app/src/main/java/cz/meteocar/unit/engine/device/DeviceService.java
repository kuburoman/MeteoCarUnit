package cz.meteocar.unit.engine.device;

import android.content.pm.PackageInfo;
import android.os.Build;

import cz.meteocar.unit.engine.ServiceManager;

/**
 * Created by Toms, 2014.
 */
public class DeviceService {

    /**
     * Získá název a výrobce android zařízení
     * @return String obsahující marketingový název a výrobce přístroje
     */
    public String getDeviceName() {

        // přečteme model a a výrobce
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        // velká písmena na začátku
        if(manufacturer != null ){
            if(!manufacturer.isEmpty()){
                manufacturer = Character.toUpperCase(manufacturer.charAt(0)) + manufacturer.substring(1);
            }
        }
        if(model != null ){
            if(!model.isEmpty()){
                model = Character.toUpperCase(model.charAt(0)) + model.substring(1);
            }
        }

        // model někdy začíná názvem výrobce
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    /**
     * Vrátí aktuální instalovanou verzi aplikace
     * @return Číselné i textové označení verze aplikace
     */
    public String getAppVersion(){

        // def. verze a proměnné
        String appVersion = "Unknown";
        int appCode;        // číselná verze
        String appName;     // textový název - může začínat i číslem, tak pozor

        // info
        try {

            // přečteme
            PackageInfo packInfo = ServiceManager.getInstance().getContext().getPackageManager().getPackageInfo(
                    ServiceManager.getInstance().getContext().getPackageName(), 0);
            appCode = packInfo.versionCode;
            appName = packInfo.versionName;

            //
            if(appName.startsWith(""+appCode)){
                appVersion = appName;
            } else {
                appVersion = appCode + " - " + appName;
            }

        } catch (Exception e){}

        // ok
        return appVersion;
    }

    /**
     * Získá textový název operačního systému zařízení a jeho verze
     * @return Název OS a verze
     */
    public String getSystemVersion(){
        return "Android " + Build.VERSION.RELEASE + "(API Level: " + Build.VERSION.SDK_INT + ")";
    }

    /*PowerManager.WakeLock fullWakeLock;
    PowerManager.WakeLock partialWakeLock;

    PowerService(){
        initWakeLocks();
    }

    // Called from onCreate
    private void initWakeLocks(){
        PowerManager powerManager = (PowerManager) ServiceManager.getInstance().getContext().getSystemService(Context.POWER_SERVICE);
        fullWakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "Loneworker - FULL WAKE LOCK");
        partialWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Loneworker - PARTIAL WAKE LOCK");
    }

    // Called implicitly when device is about to sleep or application is backgrounded
    protected void onPause(){
        super.onPause();
        partialWakeLock.acquire();
    }

    // Called implicitly when device is about to wake up or foregrounded
    protected void onResume(){
        super.onResume();
        if(fullWakeLock.isHeld()){
            fullWakeLock.release();
        }
        if(partialWakeLock.isHeld()){
            partialWakeLock.release();
        }
    }

    // Called whenever we need to wake up the device
    public void wakeDevice() {
        fullWakeLock.acquire();

        KeyguardManager keyguardManager = (KeyguardManager) ServiceManager.getInstance().getContext().getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
        keyguardLock.disableKeyguard();
    }*/
}
