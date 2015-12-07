package cz.meteocar.unit.engine.accel;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.log.AppLog;

/**
 * Created by Toms, 2014.
 */
public class AccelService implements SensorEventListener {

    public AccelService(Context ctx){
        init(ctx);
    }

    private Sensor accelerometer;

    public void init(Context ctx) {
        SensorManager sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private int counter = 0;
    private final int SUM_COUNT = 5;
    double xSum = 0;
    double ySum = 0;
    double zSum = 0;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            double x = sensorEvent.values[0];
            double y = sensorEvent.values[1];
            double z = sensorEvent.values[2];

            // spočítáme celkové přetížení
            double totalG = Math.sqrt(
                    Math.pow(x, 2) +
                            Math.pow(y, 2) +
                            Math.pow(z, 2)
            );

            // velká rána, odešleme hned event
            if(totalG > 40){

                // odešleme událost
                ServiceManager.getInstance().eventBus.post(
                        new AccelEvent(x,y,z, totalG)
                ).asynchronously();

                AppLog.i(null, "Accel BIG CRUSH");
                return;

            }

            // přičteme hodnoty
            xSum += x;
            ySum += y;
            zSum += z;
            counter++;

            // jednou za čas pošleme vyhlazený event
            if(counter >= SUM_COUNT){
                xSum /= SUM_COUNT;
                ySum /= SUM_COUNT;
                zSum /= SUM_COUNT;

                double avgTotalG = Math.sqrt(
                        Math.pow(xSum, 2) +
                                Math.pow(ySum, 2) +
                                Math.pow(zSum, 2)
                );

                // odešleme událost
                ServiceManager.getInstance().eventBus.post(
                        new AccelEvent(xSum,ySum,zSum, avgTotalG)
                ).asynchronously();
               // AppLog.i(null, "Accel avg event: "+avgTotalG);

                // vymažeme prům. hodnoty
                xSum = 0;
                ySum = 0;
                zSum = 0;
                counter = 0;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //
    }

    /**
     * Událost akcelerometru
     */
    public static class AccelEvent extends ServiceManager.AppEvent{
        private double value;
        private double x;
        private double y;
        private double z;
        public AccelEvent(double x, double y, double z, double value) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.value = value;
        }
        public double getValue() {
            return value;
        }
        public double getX() {
            return x;
        }
        public double getY() { return y; }
        public double getZ() { return z; }


        @Override
        public int getType() {
            return ServiceManager.AppEvent.EVENT_ACCEL;
        }
    }
}
