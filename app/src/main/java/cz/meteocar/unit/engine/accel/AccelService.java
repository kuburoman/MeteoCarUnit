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

    MeanFilterSmoothing filter;

    public AccelService(Context ctx) {
        init(ctx);
    }

    private Sensor accelerometer;

    public void init(Context ctx) {
        SensorManager sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        filter = new MeanFilterSmoothing();
    }

    private long frequency = 200;
    private int count = 0;

    float xSum = 0;
    float ySum = 0;
    float zSum = 0;

    long newTime = 0;
    long oldTime = 0;


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            double x = sensorEvent.values[0];
            double y = sensorEvent.values[1];
            double z = sensorEvent.values[2];

            newTime = System.currentTimeMillis();

            // přičteme hodnoty
            xSum += x;
            ySum += y;
            zSum += z;
            count++;


            // jednou za čas pošleme vyhlazený event
            if (newTime - oldTime >= frequency) {
                xSum /= count;
                ySum /= count;
                zSum /= count;

                float[] floats = filter.addSamples(new float[]{xSum,ySum,zSum});

                // odešleme událost
                ServiceManager.getInstance().eventBus.post(
                        new AccelEvent(floats[0], floats[1], floats[2])
                ).asynchronously();

                // vymažeme prům. hodnoty
                xSum = 0;
                ySum = 0;
                zSum = 0;
                count = 0;
                oldTime = newTime;
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
    public static class AccelEvent extends ServiceManager.AppEvent {
        private double x;
        private double y;
        private double z;

        public AccelEvent(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }


        @Override
        public int getType() {
            return ServiceManager.AppEvent.EVENT_ACCEL;
        }
    }
}
