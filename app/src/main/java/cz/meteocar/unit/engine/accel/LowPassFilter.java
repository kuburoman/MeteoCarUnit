package cz.meteocar.unit.engine.accel;

/**
 * Created by Nell on 4.4.2016.
 */
public class LowPassFilter {

    static final float timeConstant = 0.3f;
    private float timestamp = 0;
    private float timestampOld = 0;
    private float output[] = new float[3];

    private int count = 0;

    public float[] lowPass(float[] input) {
        timestamp = System.nanoTime();

        float dt = 1 / (count / ((timestamp - timestampOld) / 1000000000.0f));

        count++;
        timestampOld = timestamp;

        float alpha = timeConstant / (timeConstant + dt);

        output[0] = alpha * output[0] + (1 - alpha) * input[0];
        output[1] = alpha * output[1] + (1 - alpha) * input[1];
        output[2] = alpha * output[2] + (1 - alpha) * input[2];
        return output;
    }

}
