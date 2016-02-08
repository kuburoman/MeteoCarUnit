package cz.meteocar.unit.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import cz.meteocar.unit.R;

/**
 * Created by Nell on 3.2.2016.
 */
public class TripDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.trip_layout);
    }
}
