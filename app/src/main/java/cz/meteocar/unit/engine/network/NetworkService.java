package cz.meteocar.unit.engine.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.network.dto.LoginRequest;
import cz.meteocar.unit.engine.network.event.NetworkStatusEvent;
import cz.meteocar.unit.engine.network.task.CarSettingTask;
import cz.meteocar.unit.engine.network.task.DTCTask;
import cz.meteocar.unit.engine.network.task.FilterSettingTask;
import cz.meteocar.unit.engine.network.task.OBDPidsTask;
import cz.meteocar.unit.engine.network.task.PostLoginTask;
import cz.meteocar.unit.engine.network.task.PostTripTask;
import cz.meteocar.unit.engine.obd.taks.DTCRequestTask;
import cz.meteocar.unit.engine.storage.ConvertService;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.helper.TripHelper;
import cz.meteocar.unit.engine.storage.model.TripEntity;

/**
 * Created by Toms, 2014.
 */
public class NetworkService extends Thread {

    // upload
    public static final String baseURL = "http://";
    public static final String dataURL = "/data/accept";


    // thread
    private boolean threadRun = true;
    private Context context;

    private String address;
    private final String boardUnitName;
    private final String secretKey;

    // proměnné pro sledování stavu připojování
    private boolean checkConnectingStatusFlag;
    private int checkConnectingStatusRetries;
    private final int MAX_CONN_CHECK_RETRIES = 10; // 10s
    private TripHelper tripHelper;

    public boolean isConnected() {
        return checkConnectingStatusFlag;
    }

    private PostLoginTask postLoginTask;


    /**
     * Konstr., předává aplikační kontext, inicializuje fronty síťových požadavků
     *
     * @param ctx
     */
    public NetworkService(Context ctx) {
        context = ctx;
        requestQueue = new ArrayList();
        checkConnectingStatusFlag = false;
        tripHelper = ServiceManager.getInstance().db.getTripHelper();
        address = DB.get().getString("networkAddress", "http://meteocar.herokuapp.com");
        boardUnitName = "android2";
        secretKey = "Ninjahash";
        start();

        FilterSettingTask filterSettingTask = new FilterSettingTask();
        ConvertService convertService = new ConvertService();
        PostTripTask postTripTask = new PostTripTask();
        OBDPidsTask obdPidsTask = new OBDPidsTask();
        CarSettingTask carSettingTask = new CarSettingTask();
        DTCRequestTask dtcRequestTask = new DTCRequestTask();
        DTCTask dtcTask = new DTCTask();

        ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
        ScheduledFuture<?> scheduledFuture = service.scheduleAtFixedRate(postTripTask, 0, 10, TimeUnit.SECONDS);
        ScheduledFuture<?> scheduledFuture2 = service.scheduleAtFixedRate(convertService, 0, 10, TimeUnit.SECONDS);
        ScheduledFuture<?> scheduledFuture3 = service.scheduleAtFixedRate(filterSettingTask, 0, 10, TimeUnit.SECONDS);
        ScheduledFuture<?> scheduledFuture4 = service.scheduleAtFixedRate(obdPidsTask, 0, 10, TimeUnit.SECONDS);
        ScheduledFuture<?> scheduledFuture5 = service.scheduleAtFixedRate(carSettingTask, 0, 10, TimeUnit.SECONDS);
        ScheduledFuture<?> scheduledFuture6 = service.scheduleAtFixedRate(dtcRequestTask, 0, 10, TimeUnit.SECONDS);
        ScheduledFuture<?> scheduledFuture7 = service.scheduleAtFixedRate(dtcTask, 0, 10, TimeUnit.SECONDS);
    }

    public void loginUser(String username, String password) {
        new PostLoginTask().execute(new LoginRequest(username, password));
    }

    public void setAddress(String address) {
        this.address = address;
    }


    // ---------- JSON požadavky -----------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    // fronta dotazů
    private ArrayList<JSONRequest> requestQueue;

    private class JSONRequest {
        JSONRequest(String myid, String myrelativeURL, HashMap<String, String> mydata) {
            id = myid;
            relativeURL = myrelativeURL;
            data = mydata;
        }

        public String id;
        public String relativeURL;
        HashMap<String, String> data;
    }

    /**
     * Ukončí thread bezpečně
     */
    public void exit() {
        threadRun = false;
    }

    @Override
    public void run() {
        try {
            while (threadRun) {

                // flag pro kontrolu stavu připojování
                if (checkConnectingStatusFlag) {

                    // obnovíme stav připojování
                    updateConnectingStatus();

                    // usneme na půl vteřiny, bez internetu stejně nemůžeme probíhat jiná komunikace
                    try {
                        this.sleep(500);
                    } catch (Exception e) {
                        Log.e(AppLog.LOG_TAG_NETWORK, "NetowrkService.sleep() caused error.", e);
                    }
                    continue; // nesmíme nechat thread usnout, jinak by nedošlo k další kontrole
                }

//                sendTripsToServer();

                this.sleep(500);

            }
        } catch (Exception ex) {
            Log.e(AppLog.LOG_TAG_NETWORK, "Error in main thread.", ex);
        }
    }

    private void sendTripsToServer() {

        while (tripHelper.getNumberOfRecord() > 0) {
            TripEntity oneTrip = tripHelper.getOneTrip();
            if (postTripRecords(oneTrip)) {
                tripHelper.delete(oneTrip.getId());
            } else {
                return;
            }

        }

    }

    public boolean postTripRecords(TripEntity postTripRecord) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(baseURL + address + dataURL);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");

            post.setEntity(new StringEntity(postTripRecord.getJson().toString(), "UTF-8"));

            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                AppLog.i(AppLog.LOG_TAG_NETWORK, "PostTripRecords successful");
                return true;
            } else {
                AppLog.i(AppLog.LOG_TAG_NETWORK, "PostTripRecords failed");
                return false;
            }

        } catch (IOException e) {
            Log.e(AppLog.LOG_TAG_NETWORK, "Cannot send trip to server.", e);
            return false;
        }
    }

    // ---------- ZAPÍNANÍ 3G A MONITOROVANÍ KONEKTIVITY -----------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Získá a odešle event se stavem připojení na bus
     */
    public void updateNetworkStatus() {
        ServiceManager.getInstance().eventBus.post(
                getNetworkStatusEvent()
        ).asynchronously();
    }

    /**
     * Získá informace o aktivním připojení k internetu
     *
     * @return Informace o síťovém připojení, nebo null pokud není
     */
    private NetworkInfo getActiveNetworkInfo() {

        // získáme connect. manager
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        // zeptáme se na aktivní připojení
        return connManager.getActiveNetworkInfo();
    }

    /**
     * Začneme sledovat stav připojování sítě
     */
    private synchronized void startUpdatingConnectionStatus() {
        AppLog.i(null, "Starting updating net conn status");
        checkConnectingStatusRetries = 0;
        checkConnectingStatusFlag = true;
    }

    /**
     * Updatujeme stav připojení sítě z hlavního threadu
     */
    private void updateConnectingStatus() {

        // pokusy
        checkConnectingStatusRetries++;
        AppLog.i(null, "Net conn update, retry: " + checkConnectingStatusRetries);

        // získáme net info
        NetworkInfo activeNetwork = getActiveNetworkInfo();
        AppLog.i(null, "Net conn update, retry: " + checkConnectingStatusRetries);

        // už máme aktivní net
        if (activeNetwork != null) {
            //AppLog.i("Active network: "+activeNetwork.getTypeName());

            // jsme připojeni?
            if (activeNetwork.isConnected()) {

                // už jsme připojeni, ukončíme kontrolování stavu a pošle status
                checkConnectingStatusFlag = false;
                updateNetworkStatus();
                return;
            }
        } else {
            //AppLog.i("Active network NULL");
        }

        // nejsme přopojeni
        if (checkConnectingStatusRetries >= MAX_CONN_CHECK_RETRIES) {

            // ani po několika pokusech nejsme připojeni, odešleme event
            checkConnectingStatusFlag = false;
            updateNetworkStatus();
        }
    }

    public boolean isOnline() {
        NetworkInfo activeNetwork = getActiveNetworkInfo();

        if (activeNetwork != null) {
            return activeNetwork.isConnected();
        }

        return false;
    }

    /**
     * Return status of network and type of network connection.
     *
     * @return {@link NetworkStatusEvent}
     */
    private NetworkStatusEvent getNetworkStatusEvent() {

        NetworkInfo activeNetwork = getActiveNetworkInfo();

        boolean isConnected = false;
        NetworkStatus type = NetworkStatus.STATUS_NONE;

        if (activeNetwork != null) {
            type = getNetworkStatus(activeNetwork);
            isConnected = activeNetwork.isConnected();
        }

        return new NetworkStatusEvent(type, isConnected);
    }

    private NetworkStatus getNetworkStatus(NetworkInfo activeNetwork) {
        if (activeNetwork != null) {
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    return NetworkStatus.STATUS_WIFI;
                case ConnectivityManager.TYPE_MOBILE:
                    return NetworkStatus.STATUS_MOBILE;
                default:
                    return NetworkStatus.STATUS_UNKNOWN;

            }
        } else {
            return NetworkStatus.STATUS_NONE;
        }

    }

    /**
     * Aktivuje wifi
     * - poznámka: podobný kód ji může i deaktivovat (např. pro navrácení do původního stavu)
     */
    public void enableWifi() {
        AppLog.i(null, "Enabling wifi");

        //
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(true);

        // začneme updatovat stav
        startUpdatingConnectionStatus();
    }

    /**
     * Pokusí se aktivovat mobilní internet (3G nebo jiný)
     * - poznámka: dá se také deaktivovat, podobně jako u wifi
     */
    public void enableMobileNet() {

        // získáme conn. manager
        ConnectivityManager connManager;
        connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // pokusíme se to zapnout
        try {
            Method dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled",
                    boolean.class);
            dataMtd.setAccessible(true);
            dataMtd.invoke(connManager, true);

            // začneme updatovat stav
            startUpdatingConnectionStatus();
        } catch (Exception e) {
            Log.e(AppLog.LOG_TAG_NETWORK, "Mobile network cannot be started.", e);
            // nepodařilo se - odešleme event, že není připojení
            updateNetworkStatus();
        }
    }
}
