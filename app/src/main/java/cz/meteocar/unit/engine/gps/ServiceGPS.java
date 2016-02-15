package cz.meteocar.unit.engine.gps;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;

import net.engio.mbassy.bus.MBassador;

import java.util.concurrent.atomic.AtomicBoolean;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.log.AppLog;

/**
 * Created by Toms, 2014.
 */
public class ServiceGPS extends Thread implements LocationListener, GpsStatus.Listener {

    // stavy GPS
    public static final int STATUS_NO_HARDWARE = 0;
    public static final int STATUS_GPS_OFFLINE = 1;
    public static final int STATUS_NO_FIX = 2;
    public static final int STATUS_FIXED = 3;
    private int status;

    private LocationManager locationManager;
    private LocationProvider locationProvider;
    private String loctionProviderName;
    private Criteria criteria;
    private Location latestLocation;

    private AtomicBoolean locationUpdated;
    private boolean threadRun;
    private boolean threadFinalized = false;

    private Context context;
    private MBassador<ServiceManager.AppEvent> eventBus;


    public ServiceGPS(Context ctx) {
        context = ctx;

        // bool
        locationUpdated = new AtomicBoolean(false);

        // event bus
        eventBus = ServiceManager.getInstance().eventBus;

        //def. status
        status = STATUS_GPS_OFFLINE;
    }


    /**
     * Nastartuje
     */
    @Override
    public synchronized void start() {
        AppLog.i(AppLog.LOG_TAG_GPS, "GPS start()");
        threadRun = true;
        super.start();
    }

    /**
     * Běží služba?
     */
    public boolean isRunning() {
        return threadRun;
    }

    /**
     * Byla služba ukončena?
     */
    public boolean isFinalized() {
        return threadFinalized;
    }

    /**
     * Ukončí thread bezpečně
     */
    public void exit() {
        AppLog.i(AppLog.LOG_TAG_GPS, "GPS Exit reuqired");
        threadRun = false;
    }

    @Override
    public void run() {

        // inicializace
        init();

        while (threadRun) {
            //if(locationUpdated.get()){
            //AppLog.i(AppLog.LOG_TAG_GPS, "GPS C");
            try {
                synchronized (this) {
                    this.wait(1000);
                }
            } catch (InterruptedException e) {
                // spaní přerušeno, nevadí
            }

            //AppLog.i(AppLog.LOG_TAG_GPS, "GPS D");
            //latestLocation = locationManager.getLastKnownLocation(loctionProviderName);

            //AppLog.i(AppLog.LOG_TAG_GPS, "Posting location");
            eventBus.post(new GPSPositionEvent(getLocation())).asynchronously();
            // }
            //locationUpdated.set(false);
        }

        //
        AppLog.i(AppLog.LOG_TAG_GPS, "GPS E");
        threadFinalized = true;
    }

    /**
     * Inicializace GPS proměnných, nastavení listeneru polohy
     */
    private void init() {

        // podíváme se, jestli máme vůbec GPS hardware v zařízení
        boolean presentGPS = context.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
        if (!presentGPS) {
            status = STATUS_NO_HARDWARE;
        }

        // nastavení lokalizační služby
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // kritéria pro lokalizační provider (fine je jen GPS)
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        // získáme provider a přihlásíme listener
        // - tato opreace ovšem musí být volána z hlavního "looperu" / UI threadu
        final ServiceGPS thisObject = this;
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                loctionProviderName = locationManager.getBestProvider(criteria, true);
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 1, 0, thisObject);
                locationManager.addGpsStatusListener(thisObject);
            }
        });

        // můžeme také nastavit poslední známou polohu jako aktuální
        // - ale pozor, může být úplně špatně (pokud např. uživatel zapnul GPS po přejezdu někamú
        //setLocation(locationManager.getLastKnownLocation(loctionProviderName));
    }

    private synchronized void setLocation(Location location) {
        latestLocation = location;
        locationUpdated.set(true);
    }

    public Location getLocation() {
        return latestLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        //AppLog.i(AppLog.LOG_TAG_GPS, "GPS updating: " + location.toString());
        setLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int state, Bundle bundle) {
        AppLog.i(AppLog.LOG_TAG_GPS, "GPS Provider status: " + state);
        //eventBus.post(new GPSStatusEvent(i,true)).asynchronously();
        if (state == LocationProvider.OUT_OF_SERVICE) {
            status = STATUS_GPS_OFFLINE;
        }
        //if(state == LocationProvider.AVAILABLE){status = STATUS_GPS_OFFLINE;} ???????
        if (state == LocationProvider.OUT_OF_SERVICE) {
            status = STATUS_GPS_OFFLINE;
        }
        updateStatus();
    }

    @Override
    public void onProviderEnabled(String s) {

        // provider je povolený, GPS se právě zaplo
        AppLog.i(null, "GPS provider enabled");

        status = STATUS_NO_FIX;
        updateStatus();
    }

    @Override
    public void onProviderDisabled(String s) {

        // provider je vypnutý
        AppLog.i(null, "GPS provider disabled");
        status = STATUS_GPS_OFFLINE;
        updateStatus();
    }

    @Override
    public void onGpsStatusChanged(int state) {

        // gps start
        if (state == GpsStatus.GPS_EVENT_STARTED) {
            status = STATUS_NO_FIX;
        }

        // gps exit
        if (state == GpsStatus.GPS_EVENT_STOPPED) {
            status = STATUS_GPS_OFFLINE;
        }

        // první fix
        if (state == GpsStatus.GPS_EVENT_FIRST_FIX) {
            status = STATUS_FIXED;
        }

        // stav satelitu - ???
        //if(state == GpsStatus.GPS_EVENT_SATELLITE_STATUS){status = STATUS_FIXED;}


        //AppLog.i(null, "GPS Status Changed To: " + state);
        updateStatus();
    }

    /**
     * Odešle aktuální stav služby na bus
     */
    public void updateStatus() {
        //AppLog.i(null, "Reporting gps status: "+status);
        eventBus.post(new GPSStatusEvent(status)).asynchronously();
    }

    /**
     * Vrátí aktuální stav služby
     */
    public int getStatus() {
        return status;
    }

    public boolean isHardwareEnabled() {
        //AppLog.i(AppLog.LOG_TAG_GPS, "GPS Hardware enabled: "+locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Event - GPS pozice
     */
    public static class GPSPositionEvent extends ServiceManager.AppEvent {

        /**
         * GPS pozice
         */
        private Location location;

        public GPSPositionEvent(Location myLocation) {
            super();
            location = myLocation;
        }

        public Location getLocation() {
            return location;
        }

        @Override
        public int getType() {
            return ServiceManager.AppEvent.EVENT_GPS_POSITION;
        }
    }

    /**
     * Event - Stav GPS
     */
    public static class GPSStatusEvent extends ServiceManager.AppEvent {

        private int status; // stav GPS

        public GPSStatusEvent(int stat) {
            super();
            status = stat;
        }

        public int getStatus() {
            return status;
        }

        @Override
        public int getType() {
            return ServiceManager.AppEvent.EVENT_GPS_STATUS;
        }
    }
}
