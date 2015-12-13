package cz.meteocar.unit.engine.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.enums.NetworkRequestResultEnum;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.network.event.PostTripRecordsResultEvent;

/**
 * Created by Toms, 2014.
 */
public class NetworkService extends Thread {

    // network status
    public static final int STATUS_NONE = 0;
    public static final int STATUS_WIFI = 1;
    public static final int STATUS_MOBILE = 2;
    public static final int STATUS_UNKNOWN = 3;

    // upload
    public static final String baseURL = "http://www.jezdito.cz/api/";
    public static final String dataURL = "data";

    // thread
    private boolean threadRun = true;
    private Context context;

    // proměnné pro sledování stavu připojování
    private boolean checkConnectingStatusFlag;
    private int checkConnectingStatusRetries;
    private final int MAX_CONN_CHECK_RETRIES = 10; // 10s

    private ArrayList<PostTripRecord> postTripRecordsQueue;

    /**
     * Konstr., předává aplikační kontext, inicializuje fronty síťových požadavků
     *
     * @param ctx
     */
    public NetworkService(Context ctx) {
        context = ctx;
        requestQueue = new ArrayList();
        checkConnectingStatusFlag = false;
        start();
    }

    public void postTripRecords(PostTripRecord postTripRecord) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("REST API url");
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");

            post.setEntity(new StringEntity(postTripRecord.getTripRecordsJson().toString(), "UTF-8"));

            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                AppLog.i(AppLog.LOG_TAG_NETWORK, "PostTripRecords successful");
                PostTripRecordsResultEvent event = new PostTripRecordsResultEvent(NetworkRequestResultEnum.OK, postTripRecord.getTripRecordIds());
                ServiceManager.getInstance().eventBus.post(event).asynchronously();
            }else{
                AppLog.i(AppLog.LOG_TAG_NETWORK, "PostTripRecords failed");
                PostTripRecordsResultEvent event = new PostTripRecordsResultEvent(NetworkRequestResultEnum.OK, postTripRecord.getTripRecordIds());
                ServiceManager.getInstance().eventBus.post(event).asynchronously();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ---------- Bezpečnost ---------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    private int userID;
    private String userKey;

    public void setUser(int id, String key) {
        userID = id;
        userKey = key;
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

    public synchronized void sendRequest(String id, String relativeURL, HashMap<String, String> data) {
        requestQueue.add(new JSONRequest(id, relativeURL, data));
        this.notifyAll();
    }

    public synchronized void executeRequest(JSONRequest req) {

        // připravíme http klienta a požadavek na absolutní URL
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(baseURL + req.relativeURL);

        try {

            // vytvoříme nový seznam hodnot, délka je stejná jako u parametru data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(req.data.size());

            // postupně přidáme naše data
            Iterator it = req.data.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pairs = (Map.Entry) it.next();
                System.out.println(pairs.getKey() + " = " + pairs.getValue());
                nameValuePairs.add(new BasicNameValuePair(pairs.getKey(), pairs.getValue()));
                it.remove(); // pro jistotu tento pár rovnou odstraníme
            }

            // přidáme seznam hodnot do požadavku
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // odešleme požadavek a načteme odpověď
            HttpResponse httpresponse = httpclient.execute(httppost);

            // načíst odpověď po řádcích (ikdyž bude většinou jednořádková.. ale pro jistotu)
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpresponse.getEntity().getContent(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            for (String line = null; (line = reader.readLine()) != null; ) {
                builder.append(line).append("\n");
            }
            AppLog.i(AppLog.LOG_TAG_NETWORK, "Server response: " + builder.toString());

            // naparsovat JSON
            JSONTokener tokener = new JSONTokener(builder.toString());
            JSONObject response = new JSONObject(tokener);

            // oedslat event na bus
            NetworkRequestEvent evt = new NetworkRequestEvent(req.id, response);
            ServiceManager.getInstance().eventBus.post(evt).asynchronously();
            AppLog.i(AppLog.LOG_TAG_NETWORK, "ServerRequestEvent sent");

        } catch (Exception e) {
            // TODO exception handling
            e.printStackTrace();
        }
    }

    /**
     * Ukončí thread bezpečně
     */
    public void exit() {
        threadRun = false;
    }

    @Override
    synchronized public void run() {
        try {
            while (threadRun) {
                AppLog.i("Server thread running");

                // flag pro kontrolu stavu připojování
                if (checkConnectingStatusFlag) {

                    // obnovíme stav připojování
                    updateConnectingStatus();

                    // usneme na půl vteřiny, bez internetu stejně nemůžeme probíhat jiná komunikace
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        // no problem, pravděpodobně přišel nový požadavek na service
                        // a ten náš thread probudil
                    }
                    continue; // nesmíme nechat thread usnout, jinak by nedošlo k další kontrole
                }

                // požadavky na POST/JSON komunikaci
                if (!requestQueue.isEmpty()) {
                    AppLog.i("Will exec request");
                    executeRequest(requestQueue.remove(0));
                    continue;
                }

                AppLog.i("Server thread will wait");
                this.wait();
            }
        } catch (Exception ex) {
            //
        }
    }


    /**
     * Událost - načtení odpovědi na JSON požadavek
     */
    public static class NetworkRequestEvent extends ServiceManager.AppEvent {
        private String id;
        private JSONObject response;

        public NetworkRequestEvent(String myid, JSONObject myResponse) {
            id = myid;
            response = myResponse;
        }

        public String getID() {
            return id;
        }

        public JSONObject getResponse() {
            return response;
        }

        @Override
        public int getType() {
            return ServiceManager.AppEvent.EVENT_NETWORK;
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
        notifyAll();
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

    /**
     * Získá event s aktuálním stavem připojení
     *
     * @return evt
     */
    private NetworkStatusEvent getNetworkStatusEvent() {

        // zeptáme se na aktivní připojení
        NetworkInfo activeNetwork = getActiveNetworkInfo();

        //
        boolean isConnected = false;
        int type = STATUS_NONE;

        //
        if (activeNetwork != null) {
            type = STATUS_UNKNOWN;
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) type = STATUS_WIFI;
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) type = STATUS_MOBILE;
            isConnected = activeNetwork.isConnected();

        }

        //
        return new NetworkStatusEvent(type, isConnected);
    }

    public int getCurrentConnectionType() {
        // zeptáme se na aktivní připojení
        NetworkInfo activeNetwork = getActiveNetworkInfo();

        //
        boolean isConnected = false;
        int type = STATUS_NONE;

        //
        if (activeNetwork != null) {
            type = STATUS_UNKNOWN;
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) type = STATUS_WIFI;
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) type = STATUS_MOBILE;
            isConnected = activeNetwork.isConnected();

        }
        return type;
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

            // nepodařilo se - odešleme event, že není připojení
            updateNetworkStatus();
        }
    }


    /**
     * Událost - stav připojení k internetu
     */
    public static class NetworkStatusEvent extends ServiceManager.AppEvent {
        private int connectionType;
        private boolean connected;

        public NetworkStatusEvent(int connType, boolean isConn) {
            connectionType = connType;
            connected = isConn;
        }

        public int getConnectionType() {
            return connectionType;
        }

        public boolean isConnected() {
            return connected;
        }

        @Override
        public int getType() {
            return ServiceManager.AppEvent.EVENT_NETWORK;
        }
    }
}
