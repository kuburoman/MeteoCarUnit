package cz.meteocar.unit.engine.storage.simplify;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Simplify data using Douglas Peucker algorithm.
 */
public class RDPSimplify {

    public double getSquareSegmentDistance(DataPoint p0, DataPoint p1, DataPoint p2) {
        double x0, y0, x1, y1, x2, y2, dx, dy, t;

        x1 = p1.getValueX();
        y1 = p1.getValueY();
        x2 = p2.getValueX();
        y2 = p2.getValueY();
        x0 = p0.getValueX();
        y0 = p0.getValueY();

        dx = x2 - x1;
        dy = y2 - y1;

        if (dx != 0.0d || dy != 0.0d) {
            t = ((x0 - x1) * dx + (y0 - y1) * dy)
                    / (dx * dx + dy * dy);

            if (t > 1.0d) {
                x1 = x2;
                y1 = y2;
            } else if (t > 0.0d) {
                x1 += dx * t;
                y1 += dy * t;
            }
        }

        dx = x0 - x1;
        dy = y0 - y1;

        return dx * dx + dy * dy;
    }

    /**
     * Simplifies a list of points to a shorter list of points.
     *
     * @param points    original list of points
     * @param tolerance tolerance in the same measurement as the point coordinates
     * @return simplified list of points
     */
    public List<DataPoint> simplify(List<DataPoint> points,
                                    double tolerance) {

        if (points == null || points.size() <= 2) {
            return points;
        }

        double sqTolerance = tolerance * tolerance;

        return simplifyDouglasPeucker(points.toArray(new DataPoint[points.size()]), sqTolerance);
    }

    private static class Range {
        private Range(int first, int last) {
            this.first = first;
            this.last = last;
        }

        int first;
        int last;
    }

    protected List<DataPoint> simplifyDouglasPeucker(DataPoint[] points, double sqTolerance) {

        BitSet bitSet = new BitSet(points.length);
        bitSet.set(0);
        bitSet.set(points.length - 1);

        List<Range> stack = new ArrayList<>();
        stack.add(new Range(0, points.length - 1));

        while (!stack.isEmpty()) {
            Range range = stack.remove(stack.size() - 1);

            int index = -1;
            double maxSqDist = 0f;

            // find index of point with maximum square distance from first and last point
            for (int i = range.first + 1; i < range.last; ++i) {
                double sqDist = getSquareSegmentDistance(points[i], points[range.first], points[range.last]);

                if (sqDist > maxSqDist) {
                    index = i;
                    maxSqDist = sqDist;
                }
            }

            if (maxSqDist > sqTolerance) {
                bitSet.set(index);

                stack.add(new Range(range.first, index));
                stack.add(new Range(index, range.last));
            }
        }

        List<DataPoint> newPoints = new ArrayList<>(bitSet.cardinality());
        for (int index = bitSet.nextSetBit(0); index >= 0; index = bitSet.nextSetBit(index + 1)) {
            newPoints.add(points[index]);
        }

        return newPoints;
    }
}
