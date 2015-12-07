package cz.meteocar.unit.engine.network;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.model.FileObject;

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
    public static final String fileUploadURL = "upload.php";

    // thread
    private boolean threadRun = true;
    private Context context;

    // gcm
    private boolean regGCMFlag;

    // proměnné pro sledování stavu připojování
    private boolean checkConnectingStatusFlag;
    private int checkConnectingStatusRetries;
    private final int MAX_CONN_CHECK_RETRIES = 10; // 10s

    /**
     * Konstr., předává aplikační kontext, inicializuje fronty síťových požadavků
     *
     * @param ctx
     */
    public NetworkService(Context ctx) {
        context = ctx;
        fileQueue = new ArrayList();
        requestQueue = new ArrayList();
        checkConnectingStatusFlag = false;
        regGCMFlag = false;
        //sendFileToServer(null, "MySuperSpecialTypeX");
        start();
    }

    public void upload() {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("REST API url");
            post.setHeader("Content-type", "application/json");

            JSONObject obj = new JSONObject();
            obj.put("username", "un");
            obj.put("pwd", "password");
            obj.put("key", "123456");


            post.setEntity(new StringEntity(obj.toString(), "UTF-8"));

            HttpResponse response = client.execute(post);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
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


    // ---------- Upload souborů -----------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    // fronta souborů k uploadu
    private ArrayList<FileUpload> fileQueue;

    private class FileUpload {
        FileUpload(int id) {
            this.id = id;
        }

        public int id;
    }

    public synchronized void sendFileToServer(int id) {
        fileQueue.add(new FileUpload(id));
        this.notifyAll();
    }

    /**
     * TODO - uklidit upload file
     *
     * @param upload
     * @return
     */
    public boolean executeUpload(FileUpload upload) {

        // získáme z db objekt
        FileObject obj = FileObject.get(upload.id);
        if (obj == null) {
            return false;
        }

        // připravíme na http spojení
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(baseURL + fileUploadURL);
        HttpURLConnection conn;


        AppLog.i("Starting file upload");
        File file = new File(obj.getFilename());

        try {


            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";

            // ------------------ CLIENT REQUEST

            FileInputStream fileInputStream = new FileInputStream(file);

            // HEADERS
            // open a URL connection to the Servlet
            URL url = new URL(baseURL + fileUploadURL);
            conn = (HttpURLConnection) url.openConnection();// Open a HTTP connection to the URL
            conn.setDoInput(true);// Allow Inputs
            conn.setDoOutput(true);// Allow Outputs
            conn.setUseCaches(false);// Don't use a cached copy.
            conn.setRequestMethod("POST");// Use a post method.
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            // streamy
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: post-data; name=uploadedfile;filename="
                    + file.getName() + "" + lineEnd);
            dos.writeBytes(lineEnd);


            // create a buffer of maximum size
            int bytesAvailable = fileInputStream.available();
            int maxBufferSize = 1024;
            // int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[maxBufferSize];

            // read file and write it into form...
            int bytesRead = fileInputStream.read(buffer, 0, Math.min(maxBufferSize, bytesAvailable));
            while (bytesRead > 0) {
                dos.write(buffer, 0, Math.min(maxBufferSize, bytesAvailable));
                bytesAvailable = fileInputStream.available();
                bytesAvailable = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, Math.min(maxBufferSize, bytesAvailable));
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary /*+ twoHyphens*/ + lineEnd); // dvojitá čárka na konci = konec zprávy

            // ------ post variables
            dos.writeBytes("Content-Disposition: form-data; name=type" + lineEnd + lineEnd);
            dos.writeBytes(obj.getType());
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=uid" + lineEnd + lineEnd);
            dos.writeBytes("" + userID);
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=key" + lineEnd + lineEnd);
            dos.writeBytes("" + userKey);
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            //
            dos.flush();

            //read
            DataInputStream dis = new DataInputStream(conn.getInputStream());
            StringBuilder sb = new StringBuilder();
            //bytesRead = dis.write
            bytesRead = dis.read(buffer, 0, maxBufferSize);
            while (bytesRead > 0) {
                sb.append(new String(buffer, 0, bytesRead));
                AppLog.i("SB: " + sb.toString());
                if (sb.length() > 1000) {
                    break;
                }
                bytesRead = dis.read(buffer, 0, maxBufferSize);
            }
            AppLog.i("Server says: " + sb.toString());


            // close streams
            fileInputStream.close();
            dos.close();
            dis.close();

            // ok

            //můžeme smazat soubor
            if (file.delete()) {
                FileObject.delete(obj.getId());
            }

            return true;

        } catch (Exception e) {
            //fileQueue.add(upload);
            AppLog.p("File upload failed");
            e.printStackTrace();
            return false;
        }
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

                // flag pro registraci GCM
                if (regGCMFlag) {

                    // pokusíme se registrovat
                    String regID = regAtGCM();

                    // máme odpověď?
                    if (regID != null) {
                        if (!regID.equals("")) {

                            // zrušíme flag
                            regGCMFlag = false;
                            ServiceManager.getInstance().eventBus.post(
                                    new NetworkRequestEvent("gcm", new JSONObject("{gcm:'" + regID + "'}"))
                            ).asynchronously();
                        }
                    }

                    // nenecháme thread usnout
                    continue;
                }

                // požadavky na upload souboru
                if (!fileQueue.isEmpty()) {
                    AppLog.i("Will exec upload");
                    executeUpload(fileQueue.remove(0));
                    continue;
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
     * Událost - dokončení uploadu souboru
     */
    public static class NetworkFileUploadEvent extends ServiceManager.AppEvent {
        private String id;

        public NetworkFileUploadEvent(String myid) {
            id = myid;
        }

        public String getID() {
            return id;
        }

        @Override
        public int getType() {
            return ServiceManager.AppEvent.EVENT_NETWORK;
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

    // ---------- GCM ----------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Vyvolá zahájení registrace aplikace do GCM v síťovém threadu
     *
     * @param ctx Aktuální kontext aktivity
     */
    public synchronized void registerGCM(Context ctx) {
        regGCMFlag = true;
        context = ctx;
        this.notifyAll();
    }

    /**
     * Zaregistruje aplikaci u GCM pro příjem zpráv ze serveru
     * - vrací ID, které musí být předáno serveru
     *
     * @return Registration ID, pro identifikace adresáta GCM zprávy
     */
    private String regAtGCM() {

        // získáme GCM instanci
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this.context);

        // registrujeme, tím bychom měli získat ID
        String regID = null;
        try {
            regID = gcm.register(ServiceManager.GCM_SENDER_ID);
        } catch (Exception e) {

            //
            AppLog.i(AppLog.LOG_TAG_NETWORK, "GCM Registration FAILED");
            e.printStackTrace();
        }

        //ok
        return regID;
    }

    /**
     * Zruší registraci v GCM
     */
    public void unregAtGCM() {

        // získáme GCM instanci
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this.context);

        // zrušíme registraci
        try {
            gcm.unregister();
        } catch (Exception e) {
        }

    }

    public static class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            AppLog.i(AppLog.LOG_TAG_NETWORK, "Receiving GCM intent (1)");
            AppLog.i(AppLog.LOG_TAG_NETWORK, "Extras: " + intent.getExtras().toString());

            String msg = intent.getExtras().getString("msg");
            AppLog.i(AppLog.LOG_TAG_NETWORK, "Msg: " + msg);

            /*final Intent notificationIntent = new Intent(context, YourActivity.class);
            notificationIntent.setAction(Intent.ACTION_MAIN);
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);*/

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

            // probíhá připojování
            /*if(!isConnected && activeNetwork.isConnectedOrConnecting()){

                // ano probíhá připojování, vzbudíme vlákno a budeme jej pravidelně kontrolovat
                checkConnectingStatusFlag = true;
                notifyAll();
            }*/
            // nefunguje, pokud se adaptér připojuje, není vrácen jako aktivni, activeNetwork ju null

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

            // probíhá připojování
            /*if(!isConnected && activeNetwork.isConnectedOrConnecting()){

                // ano probíhá připojování, vzbudíme vlákno a budeme jej pravidelně kontrolovat
                checkConnectingStatusFlag = true;
                notifyAll();
            }*/
            // nefunguje, pokud se adaptér připojuje, není vrácen jako aktivni, activeNetwork ju null

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
