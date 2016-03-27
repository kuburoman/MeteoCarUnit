package cz.meteocar.unit.ui.activity.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;

import cz.meteocar.unit.R;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.storage.DB;

/**
 * Created by Nell on 27.3.2016.
 */
public class BoardUnitSettingActivityHelper {

    private View dialogView;
    private Context context;
    private AlertDialog dialog;

    private String boardUnitName;
    private String boardUnitSecretKey;

    /**
     * Constructor.
     *
     * @param context    Context of application.
     * @param dialogView view to be used.
     */
    public BoardUnitSettingActivityHelper(Context context, View dialogView) {
        this.context = context;
        this.dialogView = dialogView;
    }


    /**
     * Show dialog for filter edit
     */
    public void showDialog() {

        EditText name = (EditText) dialogView.findViewById(R.id.dialog_board_unit_name_edit);
        if (name != null) {
            name.setText(DB.getBoardUnitName());
        }

        EditText secretKey = (EditText) dialogView.findViewById(R.id.dialog_board_unit_secret_key_edit);
        if (secretKey != null) {
            secretKey.setText(DB.getBoardUnitSecretKey());
        }

        dialog.show();
    }

    /**
     * Init dialog for detail of filter setting.
     */
    public void initDialog() {

        // uděláme builder, nastavíme text a titulek
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.settings_board_unit_title));
        builder.setView(dialogView);

        dialog = builder
                .setPositiveButton(R.string.settings_obd_edit_btn_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setNeutralButton(R.string.settings_obd_edit_btn_save, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText name = (EditText) dialogView.findViewById(R.id.dialog_board_unit_name_edit);
                        if (name != null) {
                            boardUnitName = name.getText().toString();
                        }

                        EditText secretKey = (EditText) dialogView.findViewById(R.id.dialog_board_unit_secret_key_edit);
                        if (secretKey != null) {
                            boardUnitSecretKey = secretKey.getText().toString();
                        }

                        if (!DB.getBoardUnitName().equals(boardUnitName) || !DB.getBoardUnitSecretKey().equals(boardUnitSecretKey)) {
                            showAlertDialog();
                        } else {
                            dialog.dismiss();
                        }
                    }
                }).setCancelable(true).create();
    }

    private void hideDialog() {
        dialog.dismiss();
    }

    private void showAlertDialog() {
        new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.settings_board_unit_change))
                .setMessage(context.getResources().getString(R.string.settings_board_unit_change_message))
                .setIcon(context.getResources().getDrawable(android.R.drawable.ic_dialog_alert))
                .setPositiveButton(
                        context.getResources().getString(R.string.settings_obd_edit_btn_save),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                                hideDialog();

                                DB.setBoardUnitName(boardUnitName);
                                DB.setBoardUnitSecretKey(boardUnitSecretKey);
                                ServiceManager.getInstance().db.getDatabaseHelper().insertDefaultValues();
                            }
                        })
                .setNegativeButton(
                        context.getResources().getString(R.string.settings_obd_edit_btn_cancel),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();

                            }
                        }).show();
    }
}

