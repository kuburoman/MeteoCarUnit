package cz.meteocar.unit.engine.obd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cz.meteocar.unit.engine.log.AppLog;

/**
 * OBD Message Resolver
 * Existuje v jedné instanci vytvořené OBD Service. Zabezpečuje exekuci OBD zpráv, na streamech
 * jež byly otevřeny v OBD Service. Zprávy jsou exekuovány synchronně, proto je možné uchovávat
 * informace o poslední zprac. zprávě, aniž by hrozila chyba synchronizace.
 *
 * Created by Toms, 2014.
 */
public class OBDMessageResolver {

    /**
     * Vstupní stream z BT zařízení
     */
    private InputStream inStream;

    /**
     * Výstupní stream do BT zařízení
     */
    private OutputStream outStream;

    /**
     * StringBuilder pro zrychlení čtení zprávy
     */
    private StringBuilder readStringBuilder;

    /**
     * Init
     */
    public OBDMessageResolver(){
        readStringBuilder = new StringBuilder();
    }

    /**
     * Nastaví vstupní proud
     * @param is Vstupní stream
     */
    public void setInputStream(InputStream is){ inStream = is; }

    /**
     * Nastaví výstupní proud
     * @param os Výstupní stream
     */
    public void setOutputStream(OutputStream os){ outStream = os; }

    /**
     * Poslední odpověď zařízení (na poslední dotaz)
     */
    private String lastResponse;

    /**
     * Poslední interpretovaná odpověď zařízení
     */
    private double lastInterpretedValue;

    /**
     * Poslední zpracovaná obd zpráva
     */
    private OBDMessage lastMessage;

    /**
     * Vrátí textovou hodnotu poslední odpocědi OBD BT zařízení
     * @return Neinterpretovaná textová odpověď
     */
    public String getLastResponse(){
        return lastResponse;
    }

    /**
     * Vrátí poslední interpretovanou hodnotu
     * @return Číselná hodnota
     */
    public double getLastInterpretedValue(){
        return lastInterpretedValue;
    }

    /**
     * Odešle zprávu do OBD zařízení a načte odpověď
     * @param msg OBD zpráva
     */
    public boolean sendMessageToDeviceAndReadReply(OBDMessage msg){

        // nastavíme aktuální zprávu jako poslední
        lastMessage = msg;
        //AppLog.i("OBD sending: "+msg.getCommand());

        if(sendByteMessage(msg.getCommandByteData())){
            if(readStringResponse()){
                //AppLog.i("OBD received: "+lastResponse);

                // ověříme zprávu, pokud není OK vrátíme chybu
                if(!isValid()){ return false; }

                // interpretuje zprávu, pokud je to potřeba
                if(msg.needsInterpreting()){
                    lastInterpretedValue = msg.getValueFrom(lastResponse);
                }
                return true;
            }else{
                // TODO - failed to read message
                AppLog.p("Failed read msg response to: "+msg.getCommand());
                return false;
            }
        }else{
            // TODO - failed to send message
            AppLog.p("Failed to send msg: "+msg.getCommand());
            return false;
        }
    }

    /**
     * Odešle zprávu do výstupního proudu k zařázení
     * @param byteMsg Zpráva k odeslání
     * @return True - pokud nedošlo k vyjímce, False - pokud došlo
     */
    private boolean sendByteMessage(byte[] byteMsg){
        try {
            outStream.write(byteMsg);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    // TODO - udělat aby to po odpojení kabelu nebrečelo :-)

    /**
     * Načíst odpověd zařízení ze vstupního streamu
     * @return True - pokud nedošlo k vyjímce, False - pokud ano
     */
    private boolean readStringResponse(){

        // smazání minulé obdržené zprávy
        // objekt je znovupoužit nastavením délky na 0, data budou přepsána
        readStringBuilder.setLength(0);

        // cyklus čtení
        try {
            int data = inStream.read();
            //AppLog.i("Incoming data: "+data);
            while (data != 62) {    // '<' - smybol uznačující konec zprávy
                if ( (data != 10) && (data != 13) ) {   // vynechání znaků pro ukočení řádku
                        readStringBuilder.append((char)data);
                        //AppLog.i("Recorded data: "+data);
                        //AppLog.i("Recorded data str: "+readStringBuilder.toString());
                }
                data = inStream.read();
                //AppLog.i("Incoming data: "+data);
            }

            // přečte odpověď z builderu
            lastResponse = readStringBuilder.toString();
            return true;
        } catch (IOException e) {
            lastResponse = readStringBuilder.toString();
            AppLog.i("OBD msg - EXCEPTION while reading response");
            return false;
        }
    }

    /**
     * Známé chybové kódy zařízení
     * - převzato z práce Romana Kubů
     */
    private static final String[] errorCode = new String[]{"UNABLE TO CONNECT", "?", "ACT ALERT", "BUFFER FULL", "BUS BUSY", "ERR", "LP ALERT", "LV RESET", "NO DATA", "STOPPED", "SEARCHING"};

    /**
     * Chybový kód poslední zprávy
     * - jako index do seznamu všech chybových zpráv
     */
    private int lastErrorCode = -1;

    /**
     * Vrátí poslední chybový kód, zjištěný při volání isValid()
     * @return Chybový kód, pořadí detekované chyby v seznamu errorCode
     */
    public int getLastErrorCode(){
        return lastErrorCode;
    }

    /**
     * Zkontroluje, zda poslední odpověď neobsahovala jeden z chybových kódů
     * @return True - pokud je odpověď v pořádku, False - odpověď obsahuje chybový kód
     */
    private boolean isErrorFree(String msg){
        for (int i = 0; i < errorCode.length; i++) {
            if(msg.contains(errorCode[i])){
                lastErrorCode = i;
                return false;
            }
        }
        return true;
    }

    /**
     * Je odpověď na poslední zprávu platná
     * @return True - pokud ano, False - pokud ne
     */
    public boolean isValid(){

        // odpověď byla nulová
        if(lastResponse == null){ return false; }

        // obsahovala chybový kód?
        if(!isErrorFree(lastResponse)){ return false; }

        //
        if(lastMessage.needsInterpreting()){

            // je syntaxe odpovědi ok?
            return lastMessage.isValid();

        } else {

            // obsahuje požadovaný řetězec, který potvrdí správnost?
            if(lastMessage.getValidationString() != null) {
                return lastResponse.contains(lastMessage.getValidationString());
            }

            // žádná validace - vždy ok
            return true;
        }
    }
}
