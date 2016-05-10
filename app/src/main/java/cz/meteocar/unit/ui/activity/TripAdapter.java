package cz.meteocar.unit.ui.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.meteocar.unit.R;
import cz.meteocar.unit.engine.storage.TripDetailVO;

/**
 * Created by Nell on 7.2.2016.
 */
public class TripAdapter extends ArrayAdapter<TripDetailVO> {

    private List<TripDetailVO> objects;

    public TripAdapter(Context context, int textViewResourceId, List<TripDetailVO> objects) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
    }

    /*
     * we are overriding the getView method here - this is what defines how each
	 * list item will look.
	 */
    public View getView(int position, View convertView, ViewGroup parent) {

        // assign the view we are converting to a local variable
        View v = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.trip_list_item, null);
        }

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 *
		 * Therefore, i refers to the current Item object.
		 */
        TripDetailVO i = objects.get(position);

        if (i != null) {

            TextView startTime = (TextView) v.findViewById(R.id.trip_item_start_time);
            TextView overallTime = (TextView) v.findViewById(R.id.trip_item_overall_time);

            long overall = i.getEndTime() - i.getStartTime();

            String overallTimeValue = String.format("%d : %02d : %02d",
                    TimeUnit.MILLISECONDS.toHours(overall),
                    TimeUnit.MILLISECONDS.toMinutes(overall),
                    TimeUnit.MILLISECONDS.toSeconds(overall) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(overall))
            );

            Date date = new Date(i.getStartTime());
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());

            if (startTime != null) {
                startTime.setText(dateFormat.format(date));
            }
            if (overallTime != null) {
                overallTime.setText(overallTimeValue);
            }
        }

        // the view must be returned to our activity
        return v;

    }

}
