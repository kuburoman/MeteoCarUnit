package cz.meteocar.unit.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.engio.mbassy.listener.Handler;

import cz.meteocar.unit.R;
import cz.meteocar.unit.controller.MasterController;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.event.DebugMessageEvent;
import cz.meteocar.unit.engine.event.ErrorViewType;
import cz.meteocar.unit.engine.event.NetworkErrorEvent;
import cz.meteocar.unit.engine.gps.ServiceGPS;
import cz.meteocar.unit.engine.gps.event.GPSPositionEvent;
import cz.meteocar.unit.engine.gps.event.GPSStatusEvent;
import cz.meteocar.unit.engine.obd.OBDService;
import cz.meteocar.unit.engine.obd.event.OBDPidEvent;
import cz.meteocar.unit.engine.obd.event.OBDStatusEvent;
import cz.meteocar.unit.engine.storage.event.DBEvent;
import cz.meteocar.unit.engine.storage.helper.ObdPidHelper;
import cz.meteocar.unit.ui.UIManager;
import cz.meteocar.unit.ui.view.SpeedMeterView;

public class DashboardFragment extends Fragment {

    // view (hlavní view fragmentu)
    View view;

    // tachometry
    SpeedMeterView speedGauge;
    SpeedMeterView rpmGauge;

    // texty
    TextView timeText;
    TextView obdText;
    TextView gpsText;
    TextView infoText;

    private static int instCount = 0;

    public DashboardFragment() {
        if (instCount == 1) {
            throw new NullPointerException("return value is null at method AAA");
        }
        instCount++;
    }

    /**
     * Vytvoření fragmentu
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View kt. bylo vytvořeno dle XML
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // vytvoření view z XML
        view = inflater.inflate(R.layout.dashboard, container, false);

        // nalezení tachometrů
        speedGauge = (SpeedMeterView) view.findViewById(R.id.speedGauge);
        rpmGauge = (SpeedMeterView) view.findViewById(R.id.rpmGauge);

        // nalezení view textů
        obdText = (TextView) view.findViewById(R.id.dashboardObdText);
        gpsText = (TextView) view.findViewById(R.id.dashboardGpsText);
        timeText = (TextView) view.findViewById(R.id.dashboardTextTime);
        infoText = (TextView) view.findViewById(R.id.osmInfoText);

        // inicializace trip kontroleru
        //Button tripButton = (Button) view.findViewById(R.id.dashboardTripButton);
        //MasterController.getInstance().trip.init(tripButton);

        // zákl texty
        obdText.setText("OBD OFF");
        gpsText.setText("GPS OFF");

        //ok
        return view;
    }

    @Handler
    public void handleErrorNetworkEvent(final NetworkErrorEvent evt) {
        if (ErrorViewType.DASHBOARD.equals(evt.getView())) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), evt.getErrorResponse().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Handler
    public void handleDebugEvent(final DebugMessageEvent evt) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), evt.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Handler startu, fragment bude bezprostředně poté zobrazen
     */
    @Override
    public void onStart() {
        UIManager.getInstance().showActionBarFor(UIManager.MENU_DASHBOARD);

        // nastavíme je do tachometrů
        speedGauge.setMinMax(0, 255);
        rpmGauge.setMinMax(0, 16384);
        rpmGauge.setDisplayInThousands(true);

        // event bus
        ServiceManager.getInstance().eventBus.subscribe(this);

        // inicializace trip kontroleru
        Button tripButton = (Button) view.findViewById(R.id.dashboardTripButton);
        MasterController.getInstance().trip.init(tripButton);

        // inicializujeme video service
        // - to se zatím dělá v aktivitě

        super.onStart();
    }

    @Handler
    public void handleLocationUpdate(GPSPositionEvent msg) {
        /*AppLog.i(AppLog.LOG_TAG_GPS, "Location delivered: ");
        line1gps = "LAT: "+msg.getLocation().getLatitude();
        line2gps = "LON: "+msg.getLocation().getLongitude();
        line3gps = "ACC: "+msg.getLocation().getAccuracy();
        postInvalidate();*/

        // updatovat rychlost?
        //if(!isOBD){
        //AppLog.i(AppLog.LOG_TAG_GPS, "Updating speed (GPS): "+msg.getLocation().getSpeed());

        int value = (int) Math.round(msg.getLocation().getSpeed() * 3.6);

        //debugText.setText("S: "+value);
        speedGauge.setSecondValue(value);

        if (getView() != null) {
            getView().postInvalidate();
        }
        //}
    }

    @Handler
    public void handleGPSStatus(GPSStatusEvent msg) {
        if ((msg.getStatus() == ServiceGPS.STATUS_GPS_OFFLINE) ||
                (msg.getStatus() == ServiceGPS.STATUS_NO_HARDWARE)) {
            gpsText.setText("GPS OFF");
        }
        if ((msg.getStatus() == ServiceGPS.STATUS_NO_FIX)) {
            gpsText.setText("GPS ON");
        }
        if ((msg.getStatus() == ServiceGPS.STATUS_FIXED)) {
            gpsText.setText("GPS OK");
        }
    }

    /**
     * Handler stavu OBD
     * - používá enum typy OBDService
     *
     * @param msg
     */
    @Handler
    public void handleOBDStatus(OBDStatusEvent msg) {
        if ((msg.getStatusCode() == OBDService.OBD_STATE_NOT_INITIALIZED) ||
                (msg.getStatusCode() == OBDService.OBD_STATE_NOT_CONNECTED)) {
            obdText.setText("OBD SEARCH");
        }
        if ((msg.getStatusCode() == OBDService.OBD_STATE_CONNECTING) ||
                (msg.getStatusCode() == OBDService.OBD_STATE_RECONNECTING)) {
            obdText.setText("OBD CONN");
        }
        if ((msg.getStatusCode() == OBDService.OBD_STATE_CONNECTED)) {
            obdText.setText("OBD OK");
        }
    }


    @Handler
    public void handleOBDPID(final OBDPidEvent evt) {

        // debug
        //AppLog.i(AppLog.LOG_TAG_UI, "PID received, tag: "+evt.getMessage().getTag()+" value: "+evt.getValue()+" id: "+evt.getMessage().getID());

        // update tachometrů spustíme v UI threadu
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // speed
                if (evt.getMessage().getID() == ObdPidHelper.OBD_PID_ID_SPEED) {
                    //line6obd_speed = "S: "+evt.getValue()   + "   [ "+evt.getRawResponse()+" ]";

                    int value = (int) Math.round(evt.getValue());
                    //debugText.setText("S: "+value);
                    speedGauge.setValue(value);

                    if (getView() != null) {
                        getView().postInvalidate();
                    }

                    //AppLog.i("SPEED");
                }
                if (evt.getMessage().getID() == ObdPidHelper.OBD_PID_ID_RPM) {
                    //line6obd_rpm = "R: "+evt.getValue()       + "   [ "+evt.getRawResponse()+" ]";
                    int value = (int) Math.round(evt.getValue());
                    rpmGauge.setValue(value);
                    //AppLog.i("RPM");
                }

                //AppLog.i(null, "Time delivered: " + line4time);
            }
        });
    }


    /**
     * Handler události DB - poskytuje zákl. statistiky jízdy
     *
     * @param evt
     */
    @Handler
    public void handleDatabaseEvent(DBEvent evt) {
        //line7db = "DB count: "+evt.getCount();
        // postInvalidate();

        // debug
        //AppLog.i(AppLog.LOG_TAG_UI, "received time event: "+counter);

        // přepočítáme dobu trvání jízdy na HH : MM
        int secs = evt.getTime();
        int mins = secs / 60;
        secs -= mins * 60;
        int hours = mins / 60;
        mins -= hours * 60;

        String h = ((hours > 9) ? "" + hours : "0" + hours);
        String m = ((mins > 9) ? "" + mins : "0" + mins);
        String s = ((secs > 9) ? "" + secs : "0" + secs);
        String time = h + " : " + m + " : " + s;
        //AppLog.i(AppLog.LOG_TAG_UI, "time: "+time);

        // zobrazíme
        timeText.setText(time);
        timeText.invalidate();

        double val = Math.round((double) evt.getObdDistance() / 10);
        String distObd = "" + (val / 100);
        val = Math.round((double) evt.getGpsDistance() / 10);
        String distGps = "" + (val / 100);

        // vzdálenost
        infoText.setText(
                "" + distObd + " km\n" +
                        "[ " + distGps + " km ]"
        );
        infoText.invalidate();

        // překreslíme view
        getView().postInvalidate();
    }

}
