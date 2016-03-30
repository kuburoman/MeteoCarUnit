package cz.meteocar.unit.engine.storage.simplify;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Simplify data points based on percentage difference.
 */
public class PercentageSimplify {

    private List<DataPoint> resultList;

    private DataPoint prev;
    private DataPoint curr;
    private DataPoint next;


    public List<DataPoint> simplify(List<DataPoint> points, double reduceValue) {
        resultList = new ArrayList<>(points);
        Iterator<DataPoint> it = resultList.iterator();

        if (it.hasNext()) {
            prev = it.next();
        }

        if (it.hasNext()) {
            curr = it.next();
        }

        while (it.hasNext()) {

            // If difference is greater then given percentage point will be left in set.
            if (Math.abs(prev.getValueY() - curr.getValueY()) >= (prev.getValueY() * reduceValue * 0.01)) {

                prev.setValueY((double) Math.round(prev.getValueY()));

                prev = curr;
                curr = it.next();
                continue;
            }

            it.remove();
            next = it.next();

            if (curr.getValueY() != prev.getValueY()) {
                double currTime = next.getValueX() - curr.getValueX();
                double prevTime = curr.getValueX() - prev.getValueX();
                prev.setValueY((((prev.getValueY() * prevTime) + (curr.getValueY() * currTime)) / (currTime + prevTime)));
            }

            curr = next;
        }

        return resultList;
    }
}
