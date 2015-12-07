package cz.meteocar.unit.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.engio.mbassy.listener.Handler;

import org.json.JSONObject;

import java.util.HashMap;

import cz.meteocar.unit.R;
import cz.meteocar.unit.controller.MasterController;
import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.network.NetworkService;


public class LoginActivity extends Activity {

    private AlertDialog dialogWarning; // normální odklikávací dialog pro např. špátné heslo apod.
    private AlertDialog dialogNoInternet; // dialog když chybí internet. spoj.

    private static final String NETWORK_LOGIN_RESPONSE = "network_login_resp";

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
        initNoInternetDialog();

        // registrace na bus
        ServiceManager.getInstance().eventBus.subscribe(this);

        // update
        ServiceManager.getInstance().network.updateNetworkStatus();
    }

    /**
     * Handler "oživení" aktivity, před jakýmkoli zobrazením
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {

            // získáme ndef zprávy z karty
            Parcelable[] rawMessages = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if(rawMessages.length == 0){return;}
            NdefMessage firstMessage = (NdefMessage) rawMessages[0];
            NdefRecord record = firstMessage.getRecords()[0];

            // přečeteme text
            String textOnNFC = new String(record.getPayload());

            // dekódujeme text, přesněji, dle NFC standartu
            try{
                byte[] payload = record.getPayload(); // byty

                // získáme typ kódování
                String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";

                // získáme kód jazyka
                int languageCodeLength = payload[0] & 0077;
                String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

                // dekódujeme text
                String text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);

                textOnNFC = text;
            }catch(Exception e){
                Toast.makeText(getApplicationContext(), "NFC karta nebyla přečtena správně", Toast.LENGTH_LONG).show();
                return;
            }

            // dekódujeme JSON
            String email;
            int id;
            String name;
            try{

                // vytvoříme JSON objet a přečteme hodnoty
                JSONObject obj = new JSONObject(textOnNFC);
                email = obj.getString("email");
                id = obj.getInt("id");
                name = obj.getString("name");

            }catch(Exception e){
                Toast.makeText(getApplicationContext(), "NFC karta nebyla přečtena správně", Toast.LENGTH_LONG).show();
                return;
            }

            // předvyplníme email
            ((TextView)findViewById(R.id.nameEditText)).setText(email);

            // zobrazíme ID
            Toast.makeText(getApplicationContext(), "Karta patří uživateli: " + name, Toast.LENGTH_LONG).show();

            // technologie přiložené NFC karty
            //Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
            //Toast.makeText(getApplicationContext(), tag.toString(), Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Akce po kliknutí na tlačítko přihlásit
     */
    public void onLoginButtonClick(){
        //ServiceManager.getInstance().network.updateNetworkStatus();
        //UIManager.getInstance().showMenuActivity();
        AppLog.i("Login clicked");

        // přečteme jméno a heslo
        final String name = ((TextView)findViewById(R.id.nameEditText)).getText().toString();
        final String pwd = ((TextView)findViewById(R.id.pwdEditText)).getText().toString();

        // odešleme JSON dotaz
        ServiceManager.getInstance().network.sendRequest(
            NETWORK_LOGIN_RESPONSE, "loginUser.php", new HashMap<String, String>() {
                HashMap<String, String> init() {
                    try{
                        put("model", new JSONObject()
                            .put("name", name)
                            .put("pwd", pwd)
                            .put("login", "yes")
                            .put("isexternal", "yes")
                            .toString()
                        );
                    }catch(Exception e){/* vždy se povede */}
                    return this;
                }
            }.init()
        );
    }

    /**
     * Zpracování příchozí odpovědi na JSON dotaz
     * @param evt
     */
    @Handler
    public void handleNetworkResponse(final NetworkService.NetworkRequestEvent evt){

        AppLog.i(AppLog.LOG_TAG_NETWORK, "Response commin: "+evt.getResponse().toString());

        if(evt.getID() != NETWORK_LOGIN_RESPONSE){
            return;
        }

        // přečteme stav z JSONu
        String error = "";
        String status = "none";
        try{
            //status = "ok";
            status = evt.getResponse().getString("status");
            error = evt.getResponse().getString("error");
        }catch(Exception e){
            e.printStackTrace();
            // chyba, použijeme výchozí hodnoty
            error = "";
            status = "none";
        }

        // pokud je status ok, pokračujeme dále
        if(status.equals("ok")) {

            // přečteme údaje uživatele
            boolean jsonError = false; // došlo k chybě při parsování?
            int id = -1;
            String name = null;
            String email = null;
            String key = null;
            try{
/*                id = 10;
                name = "Roman Kubů";
                email = "kuburoman@seznam.cz";
                key = "ea74f882";*/

                id = evt.getResponse().getInt("id");
                name = evt.getResponse().getString("name");
                email = evt.getResponse().getString("email");
                key = evt.getResponse().getString("key");
            }catch(Exception e){

                // chyba
                jsonError = true;
            }

            // byla chyba parsování?
            if(!jsonError) {

                // uložit id. údaje a pokračovat na další obrazovku
                MasterController.getInstance().user.logUser(
                    id, name, email, key
                );
                MasterController.getInstance().user.checkDefaultPIDs();
                //UIManager.getInstance().showMenuActivity();
                return;

            }else{

                // chyba parsování JSONu - zobrazíme neznámou chybu
                status = "none";
                error = "";
            }
        }

        // pokud jsem tady, přihlášení se nepovedlo, připravíme hlášku ke zobrazení
        String warningText = getResources().getString(R.string.login_check_error_none);
        if(error.equals("nameempty")){
            warningText = getResources().getString(R.string.login_check_name_empty); }
        if(error.equals("pwdempty")){
            warningText = getResources().getString(R.string.login_check_pwd_empty); }
        if(error.equals("dbconnect") || error.equals("dberror")){
            warningText = getResources().getString(R.string.login_check_db_error); }
        if(error.equals("nouser")){
            warningText = getResources().getString(R.string.login_check_no_user); }
        if(error.equals("badpwd")){
            warningText = getResources().getString(R.string.login_check_bad_pwd); }

        // zobrazíme odklikávací varování
        final String finalWarningText = warningText;
        runOnUiThread(new Runnable() {
            public void run() {
                showWarningDialog(finalWarningText);
            }
        });
    }

    /**
     * Handler stavu sítě (přístup k internetu)
     * - pokud je síť nedostupná, zobrazíme varování
     * @param evt
     */
    @Handler    //(delivery = Invoke.Asynchronously, rejectSubtypes = false)
    public void handleNetworkStatusUpdate(final NetworkService.NetworkStatusEvent evt){
        /*getActivity().runOnUiThread(new Runnable() {
            public void run() {
                AppLog.i(null, "Server event delivered: " + evt.getResponse().toString());
                textView1.setText("Server: " + evt.getResponse().toString());
                textView1.postInvalidate();
            }
        });*/

        runOnUiThread(new Runnable() {
            public void run() {
                AppLog.i(null, "Network conn type: " + evt.getConnectionType());
                AppLog.i(null, "Network isConn?: " + evt.isConnected());


                // jsme připojeni?
                if (!evt.isConnected()) {

                    // ovetevřeme dialog
                    AppLog.i(null, "Network dialog show");
                    dialogNoInternet.show();
                }else{

                    // uzavřeme dialog, pokud je otevřený
                    if(dialogNoInternet.isShowing()){
                        AppLog.i(null, "Network dialog cancel");
                        dialogNoInternet.cancel();
                    }

                }
            }
        });
    }


    /**
     * Zobrazíme odklikávací dialog s požadovanou hláškou
     * @param text Hláška ke zobrazení
     */
    private void showWarningDialog(String text){
        dialogWarning.setMessage(text);
        dialogWarning.show();
    }

    /**
     * Dialog varování, pokud uživatel zadá chybné údaje
     */
    private void initWarningDialog(){

        dialogWarning = new AlertDialog.Builder(LoginActivity.this)
                .setTitle(getResources().getString(R.string.login_check_title))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // ok
                        dialog.dismiss();
                    }
                }).create();
    }

    /**
     * Připraví dialog a akce, pokud není internetové připojení
     */
    private void initNoInternetDialog(){

        // připravíme si textview
        TextView txt = new TextView(this);
        txt.setText(Html.fromHtml(getResources().getString(R.string.login_net_offline)));
        int padding = getResources().getDimensionPixelOffset(R.dimen.fragment_padding);
        txt.setPadding(padding,padding,padding,0);
        //txt.setTextSize(padding);

        // uděláme builder, nastavíme text a titulek
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle(getResources().getString(R.string.login_net_offline_title));
        builder.setView(txt);

        // přidáme tlačítka, modalitu a sestavíme
        dialogNoInternet = builder
                .setPositiveButton( R.string.login_net_btn_wifi, null)
                .setNeutralButton(R.string.login_net_btn_mob, null)
                .setNegativeButton(R.string.login_net_btn_exit, null)
                .setCancelable(false).create();

        // přidáme akce tlačítek
        // - jedná se o nestandartní způsob, umožnující dialogu zůstat otevřený i po klinutí
        // - jinak by stačilo výše při přidávání před listenery místo null
        dialogNoInternet.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog){

                // Wifi
                dialogNoInternet.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        AppLog.i(null, "Zapni wifi!");
                        ServiceManager.getInstance().network.enableWifi();
                    }
                });


                // Mobilní internet
                dialogNoInternet.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        ServiceManager.getInstance().network.enableMobileNet();
                    }
                });

                // Ukončit
                dialogNoInternet.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        ServiceManager.getInstance().exitApp();
                    }
                });

            }
        });


    }
}
