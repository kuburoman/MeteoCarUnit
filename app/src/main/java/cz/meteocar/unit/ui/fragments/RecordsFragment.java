package cz.meteocar.unit.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import cz.meteocar.unit.R;
import cz.meteocar.unit.ui.UIManager;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.log.AppLog;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the { @link Callbacks}
 * interface.
 */
public class RecordsFragment extends Fragment implements AbsListView.OnItemClickListener {

    private OnFragmentInteractionListener mListener;
    private AbsListView mListView;
    private ListAdapter mAdapter;

    //
    public static RecordsFragment newInstance(String param1, String param2) {
        RecordsFragment fragment = new RecordsFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecordsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // debug
        AppLog.log(AppLog.LOG_MSG_INFO, "Bluetooth fragment onCreate()");

        // adapter pro naplnění listu
        mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_2) {

            /**
             * Override funkce pro zjištění velikosti datového pole, složitější adaptér neumí
             * zjistit velikost sám
             * @return Počet položek ke zobrazení
             */
            @Override
            public int getCount() {
                Integer count = ServiceManager.getInstance().obd.getBluetoothDevices().size();
                AppLog.log(AppLog.LOG_MSG_INFO, "count of BT devices: "+count);
                count = (count == null) ? 0 : count;    // zaměníme null za 0
                count = (count == 0) ? 1 : count;       // zaměníme 0 za 1
                return count;
            }

            /**
             * Získání View které vykresluje konkrétní položku menu, pokud neexistuje je vytvořeno
             * @param position Pozice položky v datovém poli
             * @param convertView View které vykresluje konkrétní položku menu
             * @param parent Rodičovská skupina kam View patí
             * @return convertView
             */
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                // vytvoříme položku pokud pro danou pozici ještě není
                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(
                            android.R.layout.simple_list_item_2, null);
                }

                // objekty položky
                TextView txtName = (TextView) convertView.findViewById(android.R.id.text1);
                TextView txtAdd = (TextView) convertView.findViewById(android.R.id.text2);


                // naplnění obsahu (text a adresa)
                if(ServiceManager.getInstance().obd.getBluetoothDevices().size() == 0){
                    txtName.setText(R.string.bt_no_device_1);
                    txtAdd.setText(R.string.bt_no_device_2);
                } else {
                    BluetoothDevice device = (BluetoothDevice)ServiceManager.getInstance()
                            .obd.getBluetoothDevices().toArray()[position];

                    txtName.setText(    device.getName());
                    txtAdd.setText(     device.getAddress());
                }

                //
                return convertView;
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /*if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }*/
        UIManager.getInstance().displayToast("Item selected: "+position);
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
