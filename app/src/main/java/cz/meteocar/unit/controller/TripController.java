package cz.meteocar.unit.controller;

import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import net.engio.mbassy.listener.Handler;

import cz.meteocar.unit.R;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.gps.event.GPSPositionEvent;
import cz.meteocar.unit.engine.network.NetworkStatus;
import cz.meteocar.unit.engine.network.event.NetworkStatusEvent;
import cz.meteocar.unit.engine.obd.event.OBDPidEvent;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.network.NetworkService;
import cz.meteocar.unit.ui.UIManager;

/**
 * Created by Toms, 2014.
 */
public class TripController {

    /**
     * Je aktivní jízda?
     */
    private boolean tripActive;

    /**
     * Ovládací tlačítko
     */
    private Button button;

    /**
     * Dotaz na aktivní jízdu
     *
     * @return True pokud je jízda aktivní, False pokud ne
     */
    public boolean isActive() {
        return tripActive;
    }

    TripController() {
        tripActive = false;

        // přihlášení k odběru dat ze service busu
        ServiceManager.getInstance().eventBus.subscribe(this);
    }

    // ---------- GUI ----------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Inicializace kontrolleru
     *
     * @param btn Ovládací tlačíko na dashboardu
     */
    public void init(Button btn) {

        //
        AppLog.i(AppLog.LOG_TAG_UI, "Trip Controller init()");

        // button
        button = btn;

        // akce
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleButtonClick();
            }
        });
    }

    /**
     * Handler kliku na ovládací tlačítko
     */
    private void handleButtonClick() {

        // zastavíme nebo rozjedeme trip, podle aktuálního stavu
        if (isActive()) {
            stopTrip();
        } else {
            startTrip();
        }

    }

    /**
     * Obnoví text na ovládacím tlačíku tak, aby odpovídalo jeho aktuální akci
     */
    private void refreshButtonText() {

        // máme tlačítko?
        if (button == null) {
            AppLog.i(AppLog.LOG_TAG_UI, "Trip Controller - BUTTON NULL");
            return;
        }

        // nastavíme text podle toho, zda tlačítko jízdu ukončí nebo odstartuje
        if (isActive()) {
            button.setText(
                    UIManager.getInstance().getMenuActivity().getResources()
                            .getString(R.string.dashoard_btn_trip_end)
            );
        } else {
            button.setText(
                    UIManager.getInstance().getMenuActivity().getResources()
                            .getString(R.string.dashoard_btn_trip_start)
            );
        }
        button.postInvalidate();
    }

    // ---------- Události -----------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    @Handler
    public void handleLocationUpdate(GPSPositionEvent msg) {
        //AppLog.i(null, "Location delivered: ");
        /*line1gps = "LAT: "+msg.getLocation().getLatitude();
        line2gps = "LON: "+msg.getLocation().getLongitude();
        line3gps = "ACC: "+msg.getLocation().getAccuracy();
        postInvalidate();*/
    }

    @Handler
    public void handleOBDpid(OBDPidEvent evt) {
        /*line6obd_pid = "OBD status: "+evt.getValue();
        //AppLog.i(null, "Time delivered: " + line4time);
        postInvalidate();*/
    }

    // ---------- Start / Stop  ------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Odstartuje jízdu
     * - aktivuje záznam do DB
     * - aktivuje videozáznam
     */
    public void startTrip() {

        // flag
        tripActive = true;

        // spustíme záznam do db
        ServiceManager.getInstance().db.enableTripRecording();

        // změníme text na tlačítku
        refreshButtonText();

        // TODO tohle určitě nechceme
        // smažeme předchozí záznamy
//        TripDetailObject.deleteAllRecords();
    }

    /**
     * Ukončí trip
     */
    public void stopTrip() {

        // flag
        tripActive = false;

        // stopneme záznam do db
        ServiceManager.getInstance().db.disableTripRecording();

        // změníme text na tlačítku
        refreshButtonText();

        // vymažeme zaznamenané statistiky
        ServiceManager.getInstance().db.resetTripRecording();
    }

    // ---------- Synchronizace ------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Odešle soubory na server
     */
    public void syncFilesToServer() {

        // uživatel žádá sync
        // TODO smazat tuhle blbost
    }

    public boolean userRequestedSync = false;

    public void executeSync() {
        //
//        for (FileObject file : FileObject.getAllOfType(FileObject.TYPE_TRIP_DETAILS)) {
//            ServiceManager.getInstance().network.sendFileToServer(file.getId());
//        }
        //
//        userRequestedSync = false;
//        UIManager.getInstance().getMenuActivity().updateSyncCountMenuItem();
    }

    /**
     * Handler stavu sítě (přístup k internetu)
     * - pokud je síť nedostupná, zobrazíme varování
     *
     * @param evt
     */
    @Handler
    public void handleNetworkStatusUpdate(final NetworkStatusEvent evt) {

        // vyžádána synchronizace?
        // ne = budeme enevt ignorovat
        if (!userRequestedSync) {
            return;
        }

        UIManager.getInstance().getMenuActivity()
                .runOnUiThread(new Runnable() {
                    public void run() {
                        AppLog.i(null, "Network conn type: " + evt.getConnectionType());
                        AppLog.i(null, "Network isConn?: " + evt.isConnected());

                        // dialog
                        AlertDialog dialogNoInternet = UIManager.getInstance().getMenuActivity().dialogNoInternet;

                        // jsme připojeni?
                        if (!evt.isConnected()) {

                            // ovetevřeme dialog
                            //AppLog.i(null, "Network dialog show");
                            dialogNoInternet.show();

                        } else {

                            // uzavřeme dialog, pokud je otevřený
                            if (dialogNoInternet.isShowing()) {
                                //AppLog.i(null, "Network dialog cancel");
                                dialogNoInternet.cancel();
                            }

                        }
                    }
                });
    }

}
