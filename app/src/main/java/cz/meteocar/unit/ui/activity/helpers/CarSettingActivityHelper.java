package cz.meteocar.unit.ui.activity.helpers;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cz.meteocar.unit.R;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.enums.CarSettingEnum;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.helper.CarSettingHelper;
import cz.meteocar.unit.engine.storage.model.CarSettingEntity;
import cz.meteocar.unit.engine.task.event.RescheduleTasksEvent;

/**
 * Prepares dialog for editing filter settings.
 */
public class CarSettingActivityHelper {

    private CarSettingHelper helper = ServiceManager.getInstance().getDB().getCarSettingHelper();

    private View dialogView;
    private Context context;
    private AlertDialog dialog;
    private PreferenceScreen cat;
    private int dialogDataID = 0;

    /**
     * Constructor.
     *
     * @param context    Context of application.
     * @param dialogView view to be used.
     * @param cat        Preference screen used.
     */
    public CarSettingActivityHelper(Context context, View dialogView, PreferenceScreen cat) {
        this.context = context;
        this.dialogView = dialogView;
        this.cat = cat;
    }

    /**
     * Prepares list of filter settings.
     */
    public void createScreen() {
        cat.removeAll();

        List<CarSettingEntity> arr = helper.getAll();
        if (arr.isEmpty()) {
            cat.addPreference(new Preference(context));
            return;
        }
        for (CarSettingEntity item : arr) {

            final int myID = item.getId();
            Preference btn = new Preference(context);
            btn.setTitle(item.getCode());
            btn.setIcon(R.drawable.icon_tacho);
            btn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showDialog(myID);
                    return false;
                }
            });

            cat.addPreference(btn);
        }

    }

    /**
     * Show dialog for filter edit
     *
     * @param id of filter to edit
     */
    private void showDialog(int id) {

        dialogDataID = id;

        CarSettingEntity item = helper.get(id);

        if (item == null) {
            item = new CarSettingEntity();
            item.setId(-1);
            item.setCode("");
            item.setValue("");
            item.setActive(false);
        }

        Spinner code = (Spinner) dialogView.findViewById(R.id.dialog_car_setting_code_edit);
        if (code != null) {
            List<String> possibleTags = getPossibleTags();
            if(!"".equals(item.getCode())){
                possibleTags.add(item.getCode());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, possibleTags);
            code.setAdapter(adapter);
            if (!"".equals(item.getCode())) {
                code.setSelection(adapter.getPosition(item.getCode()));
            }
        }

        EditText roundingDecimal = (EditText) dialogView.findViewById(R.id.dialog_car_setting_value_edit);
        if (roundingDecimal != null) {
            roundingDecimal.setText(item.getValue());
        }

        dialog.show();
    }

    /**
     * Init dialog for detail of filter setting.
     */
    public void initDialog() {

        // uděláme builder, nastavíme text a titulek
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.settings_obd_edit_window_title));
        builder.setView(dialogView);

        dialog = builder
                .setNeutralButton(R.string.settings_obd_edit_btn_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();


                    }
                }).setPositiveButton(R.string.settings_obd_edit_btn_save, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        // připravíme si objekt
                        CarSettingEntity obj = new CarSettingEntity();

                        // id
                        obj.setId(dialogDataID);

                        Spinner code = (Spinner) dialogView.findViewById(R.id.dialog_car_setting_code_edit);
                        if (code != null) {
                            obj.setCode(code.getSelectedItem().toString());
                        }

                        EditText value = (EditText) dialogView.findViewById(R.id.dialog_car_setting_value_edit);
                        if (value != null) {
                            obj.setValue(value.getText().toString());
                        }

                        obj.setUpdateTime(System.currentTimeMillis());

                        // uložíme
                        try {
                            helper.save(obj);
                        } catch (DatabaseException e) {
                            Log.e(AppLog.LOG_TAG_UI, e.getMessage(), e);
                        }
                        ServiceManager.getInstance().eventBus.post(new RescheduleTasksEvent()).asynchronously();
                        dialog.dismiss();
                        createScreen();

                    }
                }).setNegativeButton(R.string.settings_obd_edit_btn_delete, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        helper.delete(dialogDataID);
                        ServiceManager.getInstance().eventBus.post(new RescheduleTasksEvent()).asynchronously();
                        createScreen();


                    }
                }).setCancelable(true).create();
    }


    protected static <E> List<String> getNames(E[] e) {
        List<String> names = new ArrayList<>();
        for (E value : e) {
            names.add(value.toString());
        }
        return names;
    }


    protected List<String> getPossibleTags() {
        List<CarSettingEntity> settings = helper.getAll();
        List<String> enums = getNames(CarSettingEnum.values());

        Iterator<String> it = enums.iterator();
        while (it.hasNext()) {
            String item = it.next();
            for (CarSettingEntity setting : settings) {
                if (item.equals(setting.getCode())) {
                    it.remove();
                    break;
                }
            }
        }

        return enums;
    }

    public void treeClick(PreferenceScreen myScreen) {

        createScreen();

        // připravíme si layout
        RelativeLayout layout = new RelativeLayout(context);
        layout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        // naplníme obsah
        Button btn = new Button(context, null, android.R.attr.buttonStyleSmall);
        btn.setText(context.getResources().getString(R.string.settings_obd_pids_add));
        btn.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        ((RelativeLayout.LayoutParams) btn.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ((RelativeLayout.LayoutParams) btn.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
        btn.setBackgroundColor(Color.TRANSPARENT);

        layout.addView(btn);
        TextView txt = new TextView(context);
        txt.setText(context.getResources().getString(R.string.settings_car_title));
        txt.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        txt.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        ((RelativeLayout.LayoutParams) txt.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        ((RelativeLayout.LayoutParams) txt.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
        int padding = context.getResources().getDimensionPixelOffset(R.dimen.fragment_padding);
        txt.setPadding(padding, padding * 3 / 4, 0, 0);
        layout.addView(txt);

        final Dialog dialog = myScreen.getDialog();

        if (dialog == null) {
            return;
        }
        dialog.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        dialog.getActionBar().setCustomView(layout);

        // přidáme akci ke tlačíku
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(-1);
            }
        });
    }
}
