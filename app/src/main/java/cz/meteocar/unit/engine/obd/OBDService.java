package cz.meteocar.unit.engine.obd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.obd.event.OBDStatusEvent;
import cz.meteocar.unit.engine.obd.event.OBDPidEvent;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.model.ObdPidEntity;

/**
 * Created by Toms, 2014.
 */
public class OBDService extends Thread {

    // singleton
    public static OBDService getInstance() {
        return ServiceManager.getInstance().obd;
    }

    // obd state codes - stavové kódy komponenty - v jakém je komponenta stavu
    public static final int OBD_STATE_NOT_INITIALIZED = 0;
    public static final int OBD_STATE_NOT_CONNECTED = 1;
    public static final int OBD_STATE_CONNECTING = 2;
    public static final int OBD_STATE_RECONNECTING = 3;
    public static final int OBD_STATE_CONNECTED = 4;
    public static final int OBD_STATE_ERROR = 5;
    public static final String[] OBD_STATE_TEXTS = new String[]{
            "OBD_STATE_NOT_INITIALIZED",
            "OBD_STATE_NOT_CONNECTED",
            "OBD_STATE_CONNECTING",
            "OBD_STATE_RECONNECTING",
            "OBD_STATE_CONNECTED",
            "OBD_STATE_ERROR"};

    // obd error codes - chybové kódy - identifikují k jaké došlo chybě
    public static final int OBD_ERROR_ALL_OK = 0;
    public static final int OBD_ERROR_NO_ADAPTER = 1;
    public static final int OBD_ERROR_ADAPTER_ENABLE_FAILED = 2;
    public static final int OBD_ERROR_NO_DEVICE = 3;
    public static final int OBD_ERROR_DEV_INIT_FAILED = 4;
    public static final String[] OBD_ERROR_TEXTS = new String[]{
            "OBD_ERROR_ALL_OK",
            "OBD_ERROR_NO_ADAPTER",
            "OBD_ERROR_ADAPTER_ENABLE_FAILED",
            "OBD_ERROR_NO_DEVICE",
            "OBD_ERROR_DEV_INIT_FAILED"};

    // obd event codes - kódy událostí komponenty - co se v komponentě právě stalo
    public static final int OBD_EVENT_ERROR = 10;
    public static final int OBD_EVENT_STATUS_CHANGE = 11;
    public static final int OBD_EVENT_DATA = 12;
    public static final int OBD_EVENT_CYCLE_COMPLETED = 13;

    //
    private int serviceStatus = OBD_STATE_NOT_INITIALIZED;
    private int lastError = OBD_ERROR_ALL_OK;

    // bluetooth
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket btSocket;   // komunikační socket mezi BT zařízeními
    private BluetoothDevice btDevice;   // vzdálené BT zařízení (OBD adaptér)
    private String btDeviceName;        // název vzdáleného BT zařízení
    private BluetoothAdapter btAdapter; // výchozí BT adaptér zařízení

    // datové proudy
    private InputStream inStream;
    private OutputStream outStream;

    // fronta zpráv
    private ArrayList<OBDMessage> queue;
    private OBDMessageResolver msgResolver;

    // thread
    private boolean threadRun = false;
    private boolean threadFinalized = false;

    // android context
    private Context context;

    // obd
    private boolean reconnectNeeded = false;


    /**
     * Vytvoří novou prázdnou službu
     */
    public OBDService(Context appContext) {
        context = appContext;

        // BT
        btSocket = null;
        btDevice = null;

        // streamy
        inStream = null;
        outStream = null;

        // fronta zpráv a resolver
        queue = new ArrayList<>();
        msgResolver = new OBDMessageResolver();

        // init hardware adaptéru
        initAdapter();
    }

    /**
     * Incializuje proměnnou adaptéru pro přístup k BT hardware
     * - tato metoda se volá z kontruktoru, neboť proměnná btAdapter je potřeba i pokud ještě
     * neběží hlavní vlákno, např. pro naplnění seznamu pro výběr BT zařízení
     */
    public void initAdapter() {

        // BT adaptér
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // ověříme přítomnost BT adaptéru v zařízení
        if (btAdapter == null) {
            AppLog.p(AppLog.LOG_TAG_OBD, "Device bluetooth adapter NOT AVAILABLE");
            lastError = OBD_ERROR_NO_ADAPTER;
            return;
        } else {
            AppLog.i(AppLog.LOG_TAG_OBD, "Device have BT adapter");
        }
    }

    /**
     * Zjištění aktuálního stavu služby
     *
     * @return Stav služby
     */
    public int getStatus() {
        return serviceStatus;
    }

    /**
     * Ověří a zapne BT adaptér asynchronně vůči volajícímu threadu
     * - trvá to dlouho, UI thread nesmí být tak dlouho blokován
     */
    public void checkAndEnableBluetoothAsynchronously() {
        (new Runnable() {
            @Override
            public void run() {
                checkAndEnableBT();
            }
        }).run();
    }


    /**
     * Zkontroluje stav BT adaptéru, případně jej zapne
     */
    private void checkAndEnableBT() {

        // ověříme jestli máme adaptér
        if (btAdapter == null) {
            lastError = OBD_ERROR_NO_ADAPTER;
            AppLog.p(AppLog.LOG_TAG_OBD, "btAdapter is NULL");
            return;
        }

        // ověříme stav BT adaptéru (vypnut/zapnut)
        if (!btAdapter.isEnabled()) {
            //Intent bluetoothEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //bluetoothEnableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //context.startActivity(bluetoothEnableIntent);

            // nebo, když máme BT admin práva

            // zapneme BT přímo
            btAdapter.enable();
        }

        // počkáme až se bt připojí
        boolean waitForAdapter = true;
        int btAdapterState;
        while (waitForAdapter) {
            try {
                this.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(AppLog.LOG_TAG_OBD, "OBDService.sleep() caused error.", e);
            }

            // zkontrolujeme status
            btAdapterState = btAdapter.getState();
            if (btAdapter.getState() != BluetoothAdapter.STATE_TURNING_ON) {
                waitForAdapter = false;
                AppLog.i(AppLog.LOG_TAG_OBD, "BT loop ended to status: " + btAdapterState);
            }
        }
        AppLog.i(AppLog.LOG_TAG_OBD, "BT state after loop: " + btAdapter.getState());

        // nyní by již měl být připojený
        if (btAdapter.isEnabled()) {
            AppLog.i(AppLog.LOG_TAG_OBD, "BT Adapter is ENABLED");
        } else {
            lastError = OBD_ERROR_ADAPTER_ENABLE_FAILED;
            AppLog.p(AppLog.LOG_TAG_OBD, "BT Adapter NOT ENABLED");
            AppLog.p(AppLog.LOG_TAG_OBD, "BT Adapter status: " + btAdapter.getState());
            return;
        }

        // počkáme na vypnutí discovery service, pokud je to potřeba
        // - tj. vyhledávací služba, skenující BT komunikační pásmo na dostupná zařízení
        // - budeme vždy používat spárované zařízení, tedy tuto službu nepotřebujeme
        // - navíc je velmi drahá z hlediska výkonu
        if (btAdapter.isEnabled()) {

            // příkaz k vypnutí discovery
            btAdapter.cancelDiscovery();

            while (btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                try {
                    this.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(AppLog.LOG_TAG_OBD, "OBDService.sleep() caused error.", e);
                }
                AppLog.p(AppLog.LOG_TAG_OBD, "BT Discovery state: " + btAdapter.getScanMode());
            }
        }

        // zkontrolujeme zda byla discovery service vypnuta
        if (btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
            AppLog.p(AppLog.LOG_TAG_OBD, "BT Discovery failed to cancel, state: " + btAdapter.getScanMode());
        } else {
            AppLog.i(AppLog.LOG_TAG_OBD, "BT Discovery OFFLINE");
        }
    }

    /**
     * Automaticky zvolí bluetooth zařízení podle názvu
     */
    private void setBTDevice() {
        for (BluetoothDevice device : btAdapter.getBondedDevices()) {
            if (device.getName().equals(btDeviceName)) {
                btDevice = device;
                AppLog.i(AppLog.LOG_TAG_OBD, "OBD BT Device selected: " + btDevice.getName());
            }
        }

        // bylo vybráno zařízení?
        if (btDevice == null) {
            AppLog.i(AppLog.LOG_TAG_OBD, "No OBD BT Device selected!!");
        }
    }


    /**
     * Vrátí seznam spárovaných BT zařízení nebo null, pokud není Bt adaptér přítomen
     *
     * @return Spárovaná BT zařízení | null
     */
    public Set<BluetoothDevice> getBluetoothDevices() {
        return (btAdapter == null) ? null : btAdapter.getBondedDevices();
    }

    /**
     * Nastaví, které spárované bluetooth zařízení se má použít
     *
     * @param index Index cílového zařízení v seznamu všech spárovaných zařízení
     */
    public void setDeviceIndex(int index) {
        btDevice = (BluetoothDevice) btAdapter.getBondedDevices().toArray()[index];
    }

    /**
     * Zaznamená do logu všechna spárovaná BT zařízení a jejich adresy
     */
    public void debugLogBluetoothDevicesList() {

        // zaznamená počet spárovaných zařízení
        AppLog.i(AppLog.LOG_TAG_OBD, "paired devices: " + btAdapter.getBondedDevices().size());

        // listujeme spárovaná zařízení
        int index = 0;
        for (BluetoothDevice dev : btAdapter.getBondedDevices()) {
            AppLog.i(AppLog.LOG_TAG_OBD, "device[" + index + "]: " + dev.getName());
            AppLog.i(AppLog.LOG_TAG_OBD, "device[" + index + "]: " + dev.getAddress());
            index++;
        }
    }

    /**
     * Počká na ukončen hlavní smyčky a potom ukončí (join-ne:) vlákno
     */
    public void safelyKillAndJoin() {

        // nastaví continue flag na false, což by mělo později ukončit cyklus vlákna
        threadRun = false;

        // počká na ukončení
        try {
            this.join();
        } catch (InterruptedException e) {
            AppLog.p(AppLog.LOG_TAG_OBD,
                    "got InterruptedException while OBD service killAndJoin()");
        }

        //
    }

    /**
     * Pokusí se otevřít spojení s BT OBD2 adaptérem
     *
     * @return True - pokud je spojení vytvořene, jinak False
     */
    public boolean connect() {

        // máme BT device, zařízení ke kterému se připojit?
        if (btDevice == null) {
            // TODO - no device error
            return false;
        }

        try {
            btSocket = btDevice.createRfcommSocketToServiceRecord(MY_UUID);
            btSocket.connect();
            inStream = btSocket.getInputStream();
            outStream = btSocket.getOutputStream();
            msgResolver.setInputStream(inStream);
            msgResolver.setOutputStream(outStream);
        } catch (IOException e) {
            Log.e(AppLog.LOG_TAG_OBD, "Failed connect to OBD", e);
            disconnectAndCleanup();
            return false;
        }
        return true;
    }

    /**
     * Odpojí aktivní spojení a vymaže proměnné
     */
    public void disconnectAndCleanup() {
        try {
            if (inStream != null) {
                inStream.close();
                inStream = null;
            }
            if (outStream != null) {
                outStream.close();
                outStream = null;
            }
            if (btSocket != null) {
                btSocket.close();
                btSocket = null;
            }
        } catch (IOException e) {
            Log.d(AppLog.LOG_TAG_OBD, "IOException while OBD service disconnect", e);
        }
    }

    /**
     * Odešle do OBD zařízení inicializační sekvenci
     * -
     *
     * @return True - pokud bylo vče ok, False - pokud došlo k chybě
     */
    private boolean sendInitialOBDSequence() {

        // vars
        boolean isOK = true;
        OBDMessage msg;
        AppLog.i(AppLog.LOG_TAG_OBD, "OBD Init seq start");

        //zahajovací sekvence
        msg = new OBDMessage("ATZ", "ELM327", false);
        if (msgResolver.sendMessageToDeviceAndReadReply(msg)) {
            AppLog.i(AppLog.LOG_TAG_OBD, msg.getCommand() + " reply: " + msgResolver.getLastResponse());
        } else {
            isOK = false;
        }
        msg = new OBDMessage("ATE0", "OK", false);
        if (msgResolver.sendMessageToDeviceAndReadReply(msg)) {
            AppLog.i(AppLog.LOG_TAG_OBD, msg.getCommand() + " reply: " + msgResolver.getLastResponse());
        } else {
            isOK = false;
        }
        msg = new OBDMessage("ATSP0", "OK", false);
        if (msgResolver.sendMessageToDeviceAndReadReply(msg)) {
            AppLog.i(AppLog.LOG_TAG_OBD, msg.getCommand() + " reply: " + msgResolver.getLastResponse());
        } else {
            isOK = false;
        }

        // handling chybového stavu
        if (!isOK) {
            AppLog.p(AppLog.LOG_TAG_OBD, "OBD Initial sequence FAILED");
        } else {
            AppLog.p(AppLog.LOG_TAG_OBD, "OBD Initial sequence OK");
        }

        // return
        return isOK;
    }

    /**
     * Nastartuje
     */
    public void setDeviceAndStart(String devName) {
        threadRun = true;
        btDeviceName = devName;
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
        threadRun = false;
    }

    /**
     * Hlavní cyklus služby
     */
    @Override
    public void run() {

        // odešleme status event - bez incializiace
        setStatusAndFireEvent(OBD_STATE_NOT_INITIALIZED);

        // ověříme stav BT afaptéru (vypnut/zapnut)
        checkAndEnableBT();

        // nastavíme BT zařízení
        setBTDevice();

        // status - nepřipojeno
        setStatusAndFireEvent(OBD_STATE_NOT_CONNECTED);

        // otevře spojení
        threadRun = connect();
        if (threadRun) {
            AppLog.i(AppLog.LOG_TAG_OBD, "AppConnected OK");
        } else {
            AppLog.p(AppLog.LOG_TAG_OBD, "Connection FAILED");
            threadFinalized = true;
            return;
        }

        //zahajovací sekvence
        if (sendInitialOBDSequence()) {
            setStatusAndFireEvent(OBD_STATE_CONNECTED);
        }

        // připravíme OBD PID dotazy
        initPIDQueue();

        // hlavní cyklus
        while (threadRun) {
            try {

                // je vyžadováno opětovné připojení?
                if (reconnectNeeded) {

                    // nastavíme status
                    setStatusAndFireEvent(OBD_STATE_RECONNECTING);

                    //zahajovací sekvence
                    if (sendInitialOBDSequence()) {

                        // připojení v pořádku
                        setStatusAndFireEvent(OBD_STATE_CONNECTED);
                        reconnectNeeded = false;

                    } else {
                        this.sleep(1000);
                        continue;
                    }
                }

                // zpracujeme celou frontu
                for (OBDMessage msg : queue) {

                    // odešleme obd zprávu
                    if (msgResolver.sendMessageToDeviceAndReadReply(msg)) {
                        firePIDEvent(msg, msgResolver.getLastInterpretedValue(), msgResolver.getLastResponse());
                    } else {
                        AppLog.i(AppLog.LOG_TAG_OBD, msg.getCommand() + " value not received :(");
                        firePIDEvent(msg, -5.0, msgResolver.getLastResponse());
                    }
                }
            } catch (Exception e) {
                Log.e(AppLog.LOG_TAG_OBD, "Error in run of OBDService", e);
            }
        }

        disconnectAndCleanup();

        threadFinalized = true;
    }

    /**
     * Inicializuje frontu PID kódů
     */
    private void initPIDQueue() {

        // přidáme všechny aktivní z DB
        for (ObdPidEntity pid : ServiceManager.getInstance().db.getObdPidHelper().getAllActive()) {
            queue.add(new OBDMessage(
                    pid.getPidCode(),
                    pid.getFormula(),
                    pid.getId(),
                    pid.getTag(),
                    pid.getName()
            ));
        }

        /*
        // rychlost
        ObdPidEntity speedPID = ObdPidEntity.get(1);
        if(speedPID == null){AppLog.i(AppLog.LOG_TAG_OBD, "speedPID NULL");}
        queue.add(new OBDMessage(
                speedPID.getPidCode(),
                speedPID.getFormula(),
                speedPID.getId(),
                speedPID.getTag(),
                speedPID.getName()
        ));

        // rpm
        ObdPidEntity rpmPID = ObdPidEntity.get(2);
        if(rpmPID == null){AppLog.i(AppLog.LOG_TAG_OBD, "rpmPID NULL");}
        queue.add(new OBDMessage(
                rpmPID.getPidCode(),
                rpmPID.getFormula(),
                rpmPID.getId(),
                rpmPID.getTag(),
                rpmPID.getName()
        ));*/
    }

    /**
     * Metoda pro snadné nastavení nového stavu služby
     * - zároven pošle event na bus
     *
     * @param newStatus Kód nového stavu
     */
    private void setStatusAndFireEvent(int newStatus) {
        serviceStatus = newStatus;
        fireStatusEvent(newStatus, OBD_STATE_TEXTS[serviceStatus]);
    }

    /**
     * Odešle pid event na bus
     *
     * @param msg   Objekt PID dotazu
     * @param value Naměřená hodnota
     */
    private void firePIDEvent(OBDMessage msg, double value, String rawResponse) {
        ServiceManager.getInstance().eventBus.post(new OBDPidEvent(msg, value, rawResponse)).asynchronously();
    }

    /**
     * Odešle status event na bus
     * - updatuje stav služby, měl by tedý být odesílám pokaždé kdy se stav služby mění
     *
     * @param statusCode Nový kód stavu služby
     * @param txt        Text popisující nový stav (pro účely ladění)
     */
    private void fireStatusEvent(int statusCode, String txt) {
        ServiceManager.getInstance().eventBus.post(new OBDStatusEvent(statusCode, txt)).asynchronously();
    }

}
