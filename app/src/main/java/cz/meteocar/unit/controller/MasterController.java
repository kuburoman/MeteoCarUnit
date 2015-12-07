package cz.meteocar.unit.controller;


/**
 * Created by Toms, 2014.
 */
public class MasterController {

    // singleton pattern
    private static final MasterController INSTANCE = new MasterController();

    public static MasterController getInstance(){
        return INSTANCE;
    }

    // controllery
    public UserController user;
    public TripController trip;

    /**
     * Konstr., instanc. kontrolerů
     */
    MasterController(){
        user = new UserController();
        trip = new TripController();
    }

    /**
     * Inicializuje kontrolery
     */
    public void init(){

        // zde nemůžeme inicializovat trip kontroller, potřebuje tlačítko
        //trip.init();

        // init user ctrlr
        user.init();
    }
}
