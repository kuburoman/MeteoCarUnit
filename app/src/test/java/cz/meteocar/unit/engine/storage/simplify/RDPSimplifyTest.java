package cz.meteocar.unit.engine.storage.simplify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link RDPSimplify}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RDPSimplifyTest {

    private RDPSimplify simplify = new RDPSimplify();

    @Test
    public void notZeroTrueTest() {
        assertTrue(simplify.notZero(2.5));
    }

    @Test
    public void notZeroFalseTest() {
        assertFalse(simplify.notZero(0.000000000005));
    }

    @Test
    public void getSquareSegmentDistanceTest() {
        DataPoint middle = new DataPoint(0, 5.0, 5.0, null);
        DataPoint first = new DataPoint(0, 0.0, 0.0, null);
        DataPoint last = new DataPoint(0, 0.0, 5.0, null);

        double squareSegmentDistance = simplify.getSquareSegmentDistance(middle, first, last);
        assertEquals(25.0, squareSegmentDistance, 0.0);
    }

    @Test
    public void getSquareSegmentDistanceTestBothNull() {
        DataPoint middle = new DataPoint(0, 0.0, 5.0, null);
        DataPoint first = new DataPoint(0, 0.0, 0.0, null);
        DataPoint last = new DataPoint(0, 0.0, 0.0, null);

        double squareSegmentDistance = simplify.getSquareSegmentDistance(middle, first, last);
        assertEquals(25.0, squareSegmentDistance, 0.0);
    }


    @Test
    public void simplifyTestFiltered() {
        List<DataPoint> dataPoints = new ArrayList<>();
        dataPoints.add(new DataPoint(0, 0.0, 0.0, null));
        dataPoints.add(new DataPoint(1, 5.0, 3.0, null));
        dataPoints.add(new DataPoint(2, 10.0, 0.0, null));

        List<DataPoint> simplify = this.simplify.simplify(dataPoints, 10);
        assertNotNull(simplify);
        assertEquals(2, simplify.size());

        assertNotNull(simplify.get(0));
        assertEquals(0, simplify.get(0).getId());

        assertNotNull(simplify.get(1));
        assertEquals(2, simplify.get(1).getId());
    }

    @Test
    public void simplifyTest() {
        List<DataPoint> dataPoints = new ArrayList<>();
        dataPoints.add(new DataPoint(0, 0.0, 0.0, null));
        dataPoints.add(new DataPoint(1, 5.0, 10.1, null));
        dataPoints.add(new DataPoint(2, 10.0, 0.0, null));

        List<DataPoint> simplify = this.simplify.simplify(dataPoints, 10);
        assertNotNull(simplify);
        assertEquals(3, simplify.size());
    }
}