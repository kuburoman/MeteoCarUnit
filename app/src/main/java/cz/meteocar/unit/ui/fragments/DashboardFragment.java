package cz.meteocar.unit.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.engio.mbassy.listener.Handler;

import java.util.ArrayList;

import cz.meteocar.unit.R;
import cz.meteocar.unit.controller.MasterController;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.helper.ObdPidHelper;
import cz.meteocar.unit.ui.UIManager;
import cz.meteocar.unit.ui.view.SpeedMeterView;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.accel.AccelService;
import cz.meteocar.unit.engine.clock.ClockService;
import cz.meteocar.unit.engine.gps.ServiceGPS;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.obd.OBDService;
import cz.meteocar.unit.engine.storage.DatabaseService;
import cz.meteocar.unit.engine.storage.model.ObdPidObject;

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

    //
    private static int instCount = 0;

    public DashboardFragment() {
        if (instCount == 1) {
            throw new NullPointerException("return value is null at method AAA");
        }
        AppLog.i(AppLog.LOG_TAG_UI, "DashboardFragment()");
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
        //timeText = (TextView) view.findViewById(R.id.dashboardTextTime);

        // inicializace trip kontroleru
        //Button tripButton = (Button) view.findViewById(R.id.dashboardTripButton);
        //MasterController.getInstance().trip.init(tripButton);

        // zákl texty
        obdText.setText("OBD OFF");
        gpsText.setText("GPS OFF");

        //ok
        return view;
    }

    /**
     * Handler startu, fragment bude bezprostředně poté zobrazen
     */
    @Override
    public void onStart() {
        AppLog.i("TestFragment onStart");
        UIManager.getInstance().showActionBarFor(UIManager.FRAGMENT_DASHBOARD);

        // získáme min a max hodnoty pro tachometry

        ArrayList<ObdPidObject> all = DB.obdPidHelper.getAll();

        ObdPidObject speed = DB.obdPidHelper.get(ObdPidHelper.OBD_PID_ID_SPEED);
        ObdPidObject rpm = DB.obdPidHelper.get(ObdPidHelper.OBD_PID_ID_RPM);

        // nastavíme je do tachometrů
        speedGauge.setMinMax(speed.getMin(), speed.getMax());
        rpmGauge.setMinMax(rpm.getMin(), rpm.getMax());
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
    public void handleLocationUpdate(ServiceGPS.GPSPositionEvent msg) {
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

        if(getView() != null) {
            getView().postInvalidate();
        }
        //}
    }

    @Handler
    public void handleGPSStatus(ServiceGPS.GPSStatusEvent msg) {
        if ((msg.getStatus() == ServiceGPS.STATUS_GPS_OFFLINE) |
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


    @Handler
    public void handleTimeUpdate(ClockService.TimeEvent msg) {
        //line4time = msg.getTime();
        //AppLog.i(null, "Time delivered: " + line4time);
        //postInvalidate();
    }

    private boolean isOBD = false;

    /**
     * Handler stavu OBD
     * - používá enum typy OBDService
     * @param msg
     */
    @Handler
    public void handleOBDStatus(OBDService.OBDEventStatus msg) {
        if ((msg.getStatusCode() == OBDService.OBD_STATE_NOT_INITIALIZED) |
                (msg.getStatusCode() == OBDService.OBD_STATE_NOT_CONNECTED)) {
            obdText.setText("OBD SEARCH");
            isOBD = false;
        }
        if ((msg.getStatusCode() == OBDService.OBD_STATE_CONNECTING) |
                (msg.getStatusCode() == OBDService.OBD_STATE_RECONNECTING)) {
            obdText.setText("OBD CONN");
            isOBD = false;
        }
        if ((msg.getStatusCode() == OBDService.OBD_STATE_CONNECTED)) {
            obdText.setText("OBD OK");
            isOBD = true;
        }
    }



    @Handler
    public void handleOBDPID(final OBDService.OBDEventPID evt){

        // debug
        //AppLog.i(AppLog.LOG_TAG_UI, "PID received, tag: "+evt.getMessage().getTag()+" value: "+evt.getValue()+" id: "+evt.getMessage().getID());

        // update tachometrů spustíme v UI threadu
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // speed
                if(evt.getMessage().getID() == ObdPidHelper.OBD_PID_ID_SPEED){
                    //line6obd_speed = "S: "+evt.getValue()   + "   [ "+evt.getRawResponse()+" ]";

                    int value = (int) Math.round(evt.getValue());
                    //debugText.setText("S: "+value);
                    speedGauge.setValue(value);

                    if(getView() != null) {
                        getView().postInvalidate();
                    }

                    //AppLog.i("SPEED");
                }
                if(evt.getMessage().getID() == ObdPidHelper.OBD_PID_ID_RPM){
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
     * @param evt
     */
    @Handler
    public void handleDatabaseEvent(DatabaseService.DBEvent evt){
        //line7db = "DB count: "+evt.getCount();
       // postInvalidate();

        // debug
        //AppLog.i(AppLog.LOG_TAG_UI, "received time event: "+counter);

        // přepočítáme dobu trvání jízdy na HH : MM
        int secs = evt.getTime();
        int mins =  secs / 60;
        secs -= mins*60;
        int hours = mins / 60;
        mins -= hours*60;

        String h = ((hours > 9) ? ""+hours : "0"+hours);
        String m = ((mins > 9) ? ""+mins : "0"+mins);
        String s = ((secs > 9) ? ""+secs : "0"+secs);
        String time = h + " : " + m + " : " + s;
        //AppLog.i(AppLog.LOG_TAG_UI, "time: "+time);

        // zobrazíme
        timeText.setText(time);
        timeText.invalidate();

        double val = Math.round((double) evt.getObdDistance() / 10);
        String distObd = ""+(val/100);
        val = Math.round((double) evt.getGpsDistance() / 10);
        String distGps = ""+(val/100);

        // vzdálenost
        infoText.setText(
            ""+distObd+" km\n"+
            "[ "+distGps+" km ]"
        );
        infoText.invalidate();

        // překreslíme view
        getView().postInvalidate();
    }

}
