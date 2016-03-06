package cz.meteocar.unit.ui.fragments;


import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import cz.meteocar.unit.R;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.TripDetailVO;
import cz.meteocar.unit.ui.activity.TripAdapter;

public class TripsListFragment extends ListFragment {
    boolean mDualPane;
    int mCurCheckPosition = 0;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String id = DB.getLoggedUser();

        ArrayList<TripDetailVO> userTripDetailList = ServiceManager.getInstance().db.getRecordHelper().getUserTripDetailList(id);

        setListAdapter(new TripAdapter(getActivity(), R.layout.trip_list_item, userTripDetailList));

        View detailsFrame = getActivity().findViewById(R.id.details);
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
        }

        if (mDualPane) {
            // In dual-pane mode, the list view highlights the selected item.
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            // Make sure our UI is in the correct state.
            showDetails(mCurCheckPosition, null);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", mCurCheckPosition);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        TripDetailVO itemAtPosition = (TripDetailVO) l.getItemAtPosition(position);
        showDetails(position, itemAtPosition);
    }

    /**
     * Helper function to show the details of a selected item, either by
     * displaying a fragment in-place in the current UI, or starting a
     * whole new activity in which it is displayed.
     */
    void showDetails(int index, TripDetailVO object) {
        mCurCheckPosition = index;

        getListView().setItemChecked(index, true);

        // Check what fragment is currently shown, replace if needed.
        DetailsFragment details = (DetailsFragment) getFragmentManager().findFragmentById(R.id.details);

        // Make new fragment to show this selection.
        details = DetailsFragment.newInstance(index, object);

        // Execute a transaction, replacing any existing fragment
        // with this one inside the frame.
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        ft.replace(R.id.details, details);

        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();


    }
}

