package cz.meteocar.unit.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.HashMap;
import java.util.List;

import cz.meteocar.unit.R;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.storage.TripDetailVO;
import cz.meteocar.unit.engine.storage.helper.filter.AccelerationVO;

public class DetailsFragment extends Fragment {

    private static HashMap<String, DetailsFragment> fragments = new HashMap<>();

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static DetailsFragment newInstance(int index, TripDetailVO itemAtPosition) {

        if (itemAtPosition != null && fragments.containsKey(itemAtPosition.getTripId())) {
            return fragments.get(itemAtPosition.getTripId());
        }

        DetailsFragment f = new DetailsFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        if (itemAtPosition != null) {
            args.putString("tripId", itemAtPosition.getTripId());
            args.putLong("startTime", itemAtPosition.getStartTime());
            args.putLong("endTime", itemAtPosition.getEndTime());
            args.putString("type", "acc123");
        }

        f.setArguments(args);
        if (itemAtPosition != null) {
            fragments.put(itemAtPosition.getTripId(), f);
        }
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (container == null) {
            return null;
        }


        View rootView = inflater.inflate(R.layout.trip_graph, container, false);


        GraphView graph = (GraphView) rootView.findViewById(R.id.graph);

        String tripId = getArguments().getString("tripId");
        String type = getArguments().getString("type");

        if (tripId == null || type == null) {
            return null;
        }

        TextView viewById = (TextView) rootView.findViewById(R.id.trip_graph_name);
        viewById.setText(tripId + " " + type);

        List<AccelerationVO> tripList = ServiceManager.getInstance().getDB().getRecordHelper().getTripByType(tripId, type);

        DataPoint[] pointsX = new DataPoint[tripList.size()];
        DataPoint[] pointsY = new DataPoint[tripList.size()];
        DataPoint[] pointsZ = new DataPoint[tripList.size()];

        for (int i = 0; i < tripList.size(); i++) {
            pointsX[i] = new DataPoint(tripList.get(i).getTime() - getArguments().getLong("startTime"), tripList.get(i).getX());
            pointsY[i] = new DataPoint(tripList.get(i).getTime() - getArguments().getLong("startTime"), tripList.get(i).getY());
            pointsZ[i] = new DataPoint(tripList.get(i).getTime() - getArguments().getLong("startTime"), tripList.get(i).getZ());
        }


        LineGraphSeries<DataPoint> seriesX = new LineGraphSeries<>(pointsX);
        LineGraphSeries<DataPoint> seriesY = new LineGraphSeries<>(pointsY);
        LineGraphSeries<DataPoint> seriesZ = new LineGraphSeries<>(pointsZ);
        graph.addSeries(seriesX);
        graph.addSeries(seriesY);
        graph.addSeries(seriesZ);

//        DataPoint[] pointsY = new DataPoint[100];
//        for (int i = 0; i < pointsY.length; i++) {
//            pointsY[i] = new DataPoint(i, Math.sin(i * 0.5) * 20 * (Math.random() * 10 + 1));
//        }
//        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>(pointsY);
//        graph.addSeries(series2);

        // enable scaling
        graph.getViewport().setScalable(true);

//        graph.getViewport().setScrollable(true);

//        graph.getViewport().setXAxisBoundsManual(true);
//        graph.getViewport().setMinX(getArguments().getLong("startTime"));
//        graph.getViewport().setMaxX(getArguments().getLong("endTime"));


        return rootView;
    }
}