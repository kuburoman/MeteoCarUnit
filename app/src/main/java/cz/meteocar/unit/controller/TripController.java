package cz.meteocar.unit.controller;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import net.engio.mbassy.listener.Handler;

import cz.meteocar.unit.R;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.gps.event.GPSPositionEvent;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.obd.event.OBDPidEvent;
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
            Log.d(AppLog.LOG_TAG_UI, "Trip Controller - BUTTON NULL");
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
        ServiceManager.getInstance().getDB().enableTripRecording();

        // změníme text na tlačítku
        refreshButtonText();
    }

    /**
     * Ukončí trip
     */
    public void stopTrip() {

        // flag
        tripActive = false;

        // stopneme záznam do db
        ServiceManager.getInstance().getDB().disableTripRecording();

        // změníme text na tlačítku
        refreshButtonText();

        // vymažeme zaznamenané statistiky
        ServiceManager.getInstance().getDB().resetTripRecording();
    }

}
