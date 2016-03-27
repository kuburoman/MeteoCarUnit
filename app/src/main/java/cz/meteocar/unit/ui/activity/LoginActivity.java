package cz.meteocar.unit.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import net.engio.mbassy.listener.Handler;

import cz.meteocar.unit.R;
import cz.meteocar.unit.controller.MasterController;
import cz.meteocar.unit.controller.UserController;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.event.ErrorViewType;
import cz.meteocar.unit.engine.event.NetworkErrorEvent;
import cz.meteocar.unit.engine.network.dto.LoginResponse;
import cz.meteocar.unit.engine.network.event.LoginEvent;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.ui.UIManager;


public class LoginActivity extends Activity {

    private AlertDialog dialogWarning;

    private static String username;
    private static String password;
    private static Boolean goToSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE); // vypnutí hlavičky - vrchní řádku app
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // opravuje problém s přetrvávající softw. klávesnicí
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        //
        setContentView(R.layout.activity_login);

        // tlačítko přihlásit
        Button btn = (Button) findViewById(R.id.btnlogin);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onLoginButtonClick();
            }
        });

        //
        TextView t2 = (TextView) findViewById(R.id.login_note_view);
        t2.setMovementMethod(LinkMovementMethod.getInstance());

        //

        // dialogy
        initWarningDialog();

        // registrace na bus
        ServiceManager.getInstance().eventBus.subscribe(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Akce po kliknutí na tlačítko přihlásit
     */
    public void onLoginButtonClick() {
        username = ((TextView) findViewById(R.id.nameEditText)).getText().toString();
        password = ((TextView) findViewById(R.id.pwdEditText)).getText().toString();
        goToSettings = ((CheckBox) findViewById(R.id.settingCheckbox)).isChecked();

        if (goToSettings && validateBoardUnit(username, password)) {
            UIManager.getInstance().showSettingsActivity();
            return;
        }


        if (ServiceManager.getInstance().network.isOnline()) {
            ServiceManager.getInstance().network.loginUser(username, password);
            return;
        }
        if (MasterController.getInstance().user.verifyUser(username, password)) {
            boolean isAdmin = MasterController.getInstance().user.isUserAdmin(username);
            ServiceManager.getInstance().eventBus.post(new LoginEvent(new LoginResponse("OK", isAdmin))).asynchronously();
            return;
        }

        if (goToSettings && validateBoardUnit(username, password)) {
            UIManager.getInstance().showSettingsActivity();
            return;
        }

        showWarningDialog("User not found locally.");

    }


    /**
     * Zpracování příchozí odpovědi na JSON dotaz
     *
     * @param evt
     */
    @Handler
    public void handleLoginEvent(final LoginEvent evt) {

        if (goToSettings) {
            if (evt.getResponse().getIsAdmin() || validateBoardUnit(username, password)) {
                UIManager.getInstance().showSettingsActivity();
                return;
            }
        }


        DB.setLoggedUser(username);

        MasterController.getInstance().user.updateUser(username, password, evt.getResponse().getIsAdmin());

        DB.set().putBoolean(UserController.SETTINGS_KEY_OBD_PIDS_SET, true).commit();

        UIManager.getInstance().showMenuActivity();
    }

    @Handler
    public void handleErrorNetworkEvent(final NetworkErrorEvent evt) {
        if (ErrorViewType.LOGIN.equals(evt.getView())) {
            runOnUiThread(new Runnable() {
                public void run() {
//                    Toast.makeText(LoginActivity.this, evt.getErrorResponse().getMessage(), Toast.LENGTH_SHORT).show();
                    showWarningDialog(evt.getErrorResponse().getMessage());
                }
            });
        }
    }

    private boolean validateBoardUnit(String name, String secretKey) {
        return DB.getBoardUnitName().equals(name) && DB.getBoardUnitSecretKey().equals(secretKey);
    }

    /**
     * Zobrazíme odklikávací dialog s požadovanou hláškou
     *
     * @param text Hláška ke zobrazení
     */
    private void showWarningDialog(String text) {
        dialogWarning.setMessage(text);
        dialogWarning.show();
    }

    /**
     * Dialog varování, pokud uživatel zadá chybné údaje
     */
    private void initWarningDialog() {

        dialogWarning = new AlertDialog.Builder(LoginActivity.this)
                .setTitle(getResources().getString(R.string.login_check_title))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
    }
}
