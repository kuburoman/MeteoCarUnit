package cz.meteocar.unit.engine.accel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class MeanFilterSmoothing {

    private float timeConstant = 1;
    private float startTime = 0;
    private int count = 0;
    private int filterWindow = 20;

    private boolean dataInit;

    private ArrayList<LinkedList<Number>> dataLists;

    /**
     * Initialize a new MeanFilter object.
     */
    public MeanFilterSmoothing() {
        dataLists = new ArrayList<>();
        dataInit = false;
    }

    /**
     * Filter the data.
     *
     * @return the filtered output data.
     */
    public float[] addSamples(float[] data) {
        // Initialize the start time.
        if (Float.floatToRawIntBits(startTime) == 0) {
            startTime = System.nanoTime();
        }

        float timestamp = System.nanoTime();

        float hz = count++ / ((timestamp - startTime) / 1000000000.0f);

        filterWindow = (int) (hz * timeConstant);

        for (int i = 0; i < data.length; i++) {
            // Initialize the data structures for the data set.
            if (!dataInit) {
                dataLists.add(new LinkedList<Number>());
            }

            dataLists.get(i).addLast(data[i]);

            if (dataLists.get(i).size() > filterWindow) {
                dataLists.get(i).removeFirst();
            }
        }

        dataInit = true;

        float[] means = new float[dataLists.size()];

        for (int i = 0; i < dataLists.size(); i++) {
            means[i] = getMean(dataLists.get(i));
        }

        return means;
    }

    /**
     * Get the mean of the data set.
     *
     * @param data the data set.
     * @return the mean of the data set.
     */
    private float getMean(List<Number> data) {
        float m = 0;
        float countData = 0;

        for (int i = 0; i < data.size(); i++) {
            m += data.get(i).floatValue();
            countData++;
        }

        if (Float.floatToRawIntBits(countData) != 0) {
            m = m / countData;
        }

        return m;
    }

}
