package cz.meteocar.unit.engine.obd;

/**
 * Objekt reprezentující zprávu průběžně zasílanou do OBD zařízení
 * - jedna instance pro jeden typ dotazu
 * - data v instanci jsou průběžně obnovována OBDService třídou
 * TODO - přidat čas do zpráv, pro integrování
 * Created by Toms, 2014.
 */
public class OBDMessage {

    // ID (get/set)
    private int ID = -1;

    // další údaje (get/set)
    private String name;


    private String tag;

    // příkaz a interpretace / validace
    private String command = null;
    private byte[] commandByteData = null;
    private FormulaInterpreter interpret = null;
    private String validationString = null;

    public OBDMessage() {
    }

    OBDMessage(String obdCommand, String formulaOrValidationString, boolean isFormula) {
        setCommand(obdCommand);
        init(isFormula, formulaOrValidationString);
    }

    OBDMessage(String obdCommand, String formula) {
        setCommand(obdCommand);
        init(true, formula);
    }

    OBDMessage(String obdCommand, String formula, int mID, String tag, String name) {
        setID(mID);
        setCommand(obdCommand);
        setName(name);
        setTag(tag);
        init(true, formula);
    }

    private void init(boolean useFormula, String formulaOrValidationString) {
        if (useFormula) {
            setFormula(formulaOrValidationString);
        } else {
            validationString = formulaOrValidationString;
        }
    }

    /**
     * (Pře)nastaví kód OBD příkazu a přepočítá bytové pole příkazu
     *
     * @param newCommand Nový příkaz odesílaný objektem
     */
    public void setCommand(String newCommand) {
        command = newCommand;
        commandByteData = ((String) command + "\r").getBytes();
    }

    /**
     * Získá OBD příkaz zprávy
     *
     * @return OBD kód
     */
    public String getCommand() {
        return command;
    }

    /**
     * Vrátí datové pole, které se odešle jako příkaz do BT OBD zařízení
     *
     * @return Bytové pole
     */
    public byte[] getCommandByteData() {
        return commandByteData;
    }

    /**
     * Vrátí řetězec pro ověření korektnosti přijaté odpovědi
     *
     * @return String, který by se měl v odpovědi nacházet
     */
    public String getValidationString() {
        return validationString;
    }

    /**
     * Nastaví vzorec pro výpočet číselné hodnoty z binární odpovědi
     *
     * @param formula Vzorec pro interpretaci dat
     */
    public void setFormula(String formula) {
        interpret = new FormulaInterpreter(formula);
    }

    /**
     * Má být zpráva interpretována?
     * - např. výsledky zpráv kontrolovy protokolu nepotřebují být interpretovány
     * - PID zprávy to potřebují
     * Interně tyto dva druhy zpráv odlišíme přítomností nebo absencí interpreteru
     *
     * @return True pokud ano, False pokud ne
     */
    public boolean needsInterpreting() {
        return interpret != null;
    }

    /**
     * Zjistí zda je zpráva platná
     *
     * @return
     */
    public boolean isValid() {
        return !needsInterpreting() || interpret.isSyntaxOK();
    }



    /**
     * Interpretuje zjištěnou hodnotu
     *
     * @param response Textová odezva OBD zařízení
     * @return Interpretovaná číselná hodnota
     */
    public double getValueFrom(String response) {
        return interpret.interpretString(response);
    }

    public void setID(int mID) {
        ID = mID;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
