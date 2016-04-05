package cz.meteocar.unit.engine.accel;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.accel.event.AccelerationEvent;

/**
 * Acceleration service.
 */
public class AccelerationService implements SensorEventListener {

    private MeanFilterSmoothing filter;

    public AccelerationService(Context ctx) {
        init(ctx);
    }

    private Sensor accelerometer;
    private float[] gravity = new float[3];

    public void init(Context ctx) {
        SensorManager sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        filter = new MeanFilterSmoothing();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            final float alpha = 0.8f;

//             Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];

//             Remove the gravity contribution with the high-pass filter.
            float x = sensorEvent.values[0] - gravity[0];
            float y = sensorEvent.values[1] - gravity[1];
            float z = sensorEvent.values[2] - gravity[2];

            float[] floats = filter.addSamples(new float[]{x, y, z});

            ServiceManager.getInstance().eventBus.post(
                    new AccelerationEvent(floats[0], floats[1], floats[2])
            ).asynchronously();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //
    }
}
