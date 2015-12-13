package cz.meteocar.unit.engine;

/**
 * Created by Nell on 12.12.2015.
 */
public class TripService extends Thread {


    private static final TripService MY_TRIP_SERVICE = new TripService();

    public static TripService getInstance() {
        return MY_TRIP_SERVICE;
    }

    @Override
    public void run() {
        super.run();
    }
}
