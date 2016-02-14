package cz.meteocar.unit.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.engio.mbassy.listener.Handler;

import java.util.ArrayList;
import java.util.HashMap;

import cz.meteocar.unit.R;
import cz.meteocar.unit.controller.UserController;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.network.NetworkService;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.model.ObdPidObject;

public class DebugFragment extends Fragment {

    View view;
    TextView textView1;
    ArrayList<Button> buttons;
    RelativeLayout fragmentLayout;

    /**
     * Prázdný kontruktor
     */
    public DebugFragment() {
        buttons = new ArrayList();
    }

    /**
     * Test
     */
    private void BUTTON_TEST() {
        AppLog.i(null, "Klik babe");
        textView1.setText("Kliknuto!");
    }

    /**
     * Odhlášení uživatele
     */
    private void BUTTON_LOG_OFF() {
        textView1.setText("Uživatel odhlášen");
        DB.set().putBoolean(UserController.SETTINGS_KEY_USER_LOGGED, false).commit();
    }

    /**
     * Smazání záznamu jízdy
     */
    private void BUTTON_ERASE_TRIP_DETAILS() {
        DB.obdPidHelper.deleteAll();
        textView1.setText("Záznamy vymazány");
    }

    /**
     * Zaloguje všechny pidy z databáze
     */
    private void BUTTON_LOG_PIDS() {
        ArrayList<ObdPidObject> arr = DB.obdPidHelper.getAll();
        StringBuilder sb = new StringBuilder();
        sb.append("OBD PIDs count: " + arr.size() + "\n");
        for (ObdPidObject obj : arr) {
            sb.append("ID: " + obj.getId() + " tag: " + obj.getTag() + " code: " + obj.getPidCode() + " formula: " + obj.getFormula() + "\n");
        }
        textView1.setText(sb.toString());
    }

    /**
     * Uložení záznamu do souboru
     */
    private void BUTTON_NUKE_TRIPS() {

        // všechny aktuální záznamy
//        TripDetailObject.deleteAllRecords();

        // všechny záznamy v souborech
//        FileObject.deleteAllRecords();
//
//        // všechny soubory
//        File tripsDir = new File(FileSystem.getTripLogDir());
//        for (File file : tripsDir.listFiles()) file.delete();

    }

    /**
     * Pošle uložený zaáznam na server
     */
    private void BUTTON_SEND_FILE() {
        //DB.tripHelper.saveToFile();
        //DB.tripHelper.deleteAllRecords();
    }

    /**
     * Pošle uložený zaáznam na server
     */
    private void BUTTON_ERASE_PIDS() {
        DB.obdPidHelper.deleteAll();
        DB.set().putBoolean(UserController.SETTINGS_KEY_OBD_PIDS_SET, false).commit();
        textView1.setText("PIDy smazány");
    }

    /**
     * Vytvoření nebo obnovení view
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @SuppressWarnings("ResourceType")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // mezery
        final int PADDING = getResources().getDimensionPixelOffset(R.dimen.fragment_padding);
        final int TOP_MARGIN = getResources().getDimensionPixelOffset(R.dimen.action_bar_anim_offset);

        // layout
        fragmentLayout = new RelativeLayout(getActivity());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, TOP_MARGIN, 0, 0);
        fragmentLayout.setLayoutParams(layoutParams);
        fragmentLayout.setPadding(PADDING, PADDING, PADDING, PADDING);
        view = fragmentLayout;

        // txt
        textView1 = new TextView(getActivity());
        textView1.setId(1);
        textView1.setText("Obrazovka ladění");
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        textView1.setLayoutParams(params);
        fragmentLayout.addView(textView1);

        // tlačítka
        addButton("test.php", new View.OnClickListener() {
            public void onClick(View v) {
                BUTTON_TEST();
            }
        });
        addButton("Odhlásit uživatele", new View.OnClickListener() {
            public void onClick(View v) {
                BUTTON_LOG_OFF();
            }
        });
        addButton("Smazat záznam jízdy", new View.OnClickListener() {
            public void onClick(View v) {
                BUTTON_ERASE_TRIP_DETAILS();
            }
        });
        addButton("Smazat všechny jízdy", new View.OnClickListener() {
            public void onClick(View v) {
                BUTTON_NUKE_TRIPS();
            }
        });
        addButton("Odeslat soubor na server", new View.OnClickListener() {
            public void onClick(View v) {
                BUTTON_SEND_FILE();
            }
        });
        addButton("Zobrazit PIDy do logu", new View.OnClickListener() {
            public void onClick(View v) {
                BUTTON_LOG_PIDS();
            }
        });
        addButton("Smazat PIDy", new View.OnClickListener() {
            public void onClick(View v) {
                BUTTON_ERASE_PIDS();
            }
        });

        // přihlášení k odběru dat ze service busu
        ServiceManager.getInstance().eventBus.subscribe(this);

        return view;
    }

    /**
     * Obnovení obrazovky
     */
    @Override
    public void onResume() {
        super.onResume();

        // obnovíme text
        if (textView1 != null) {
            textView1.setText("Obrazovka ladění");
        }

    }

    /**
     * Poslední přidaný řádek
     */
    private LinearLayout actualLinLayout;

    /**
     * Přidá tlačítko
     *
     * @param buttonText
     * @param clickListener
     */
    private void addButton(String buttonText, View.OnClickListener clickListener) {

        // padding - vzdálenost od předchozího řádku
        int PADDING = getResources().getDimensionPixelOffset(R.dimen.fragment_padding);

        // btn
        Button btn = new Button(getActivity());

        // za  každým sudým nový řádek
        if (buttons.size() % 2 == 0) {

            LinearLayout newLinLayout = new LinearLayout(getActivity());
            newLinLayout.setId(buttons.size() + 10);
            fragmentLayout.addView(newLinLayout);

            // layout parametry pro řídek
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, PADDING, 0, 0);

            // prvek pod, kt. se tlačítko vykreslí
            if (buttons.size() == 0) {
                params.addRule(RelativeLayout.BELOW, textView1.getId());
            } else {
                params.addRule(RelativeLayout.BELOW, actualLinLayout.getId());
            }

            // nastavit parametry novému řádku
            newLinLayout.setLayoutParams(params);

            // nahradit strý řádek
            actualLinLayout = newLinLayout;
        }

        // layout parametry pro tlačítko
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (buttons.size() % 2 == 0) {
            params.gravity = Gravity.LEFT;
            params.weight = 0.5f;
        } else {
            params.gravity = Gravity.RIGHT;
            params.weight = 0.5f;
        }
        params.setMargins(0, 0, 0, 0);
        btn.setLayoutParams(params);

        // přidáme tlačítko a nastavíme jeho parametry
        actualLinLayout.addView(btn);
        btn.setId(buttons.size() + 2);
        btn.setText(buttonText);
        btn.setOnClickListener(clickListener);

        // přidáme do arraylistu
        buttons.add(btn);
    }

    @Handler    //(delivery = Invoke.Asynchronously, rejectSubtypes = false)
    public void handleLocationUpdate(final NetworkService.NetworkRequestEvent evt) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                /*AppLog.i(null, "Server event delivered: " + evt.getResponse().toString());
                textView1.setText("Server: " + evt.getResponse().toString());
                textView1.postInvalidate();*/
            }
        });
    }
}
