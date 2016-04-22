package cz.meteocar.unit;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

import cz.meteocar.unit.ui.activity.SplashActivity;

/**
 * Created by Nell on 20.4.2016.
 */
public class LoginActivityTest extends ActivityInstrumentationTestCase2<SplashActivity> {

    private Solo solo;

    public LoginActivityTest() {
        super(SplashActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        solo = new Solo(getInstrumentation());
        Context context = getInstrumentation().getTargetContext();
        context.deleteDatabase(BuildConfig.DATABASE);
        SharedPreferences settings = context.getSharedPreferences("appSettings", Context.MODE_MULTI_PROCESS);
        settings.edit().clear().commit();

        getActivity();
    }

    @Override
    public void tearDown() throws Exception {

        solo.finishOpenedActivities();
    }

    public void testServerAddress() throws Exception {
        //Unlock the lock screen
        solo.unlockScreen();

        LoginHelper.loginToSettings(solo);
        solo.sleep(200);

        solo.clickOnText("Nastavení sítě");
        solo.clickOnText("Adresa serveru");
        solo.clearEditText(0);
        solo.enterText(0, "http://localhost:9000");
        solo.clickOnButton("OK");
        solo.waitForText("Adresa serveru");
        solo.goBack();
        solo.goBack();

        LoginHelper.loginToSettings(solo);

        solo.clickOnText("Nastavení sítě");
        solo.clickOnText("Adresa serveru");
        assertTrue(solo.searchText("http://localhost:9000"));
        solo.clickOnText("OK");
        solo.goBack();
        solo.goBack();
    }

    public void testFilterSettingsDelete() throws Exception {
        //Unlock the lock screen
        solo.unlockScreen();

        LoginHelper.loginToSettings(solo);
        solo.sleep(200);

        solo.clickOnText("Nastavení filtru");
        solo.clickOnText("obd_speed");
        solo.clickOnView(solo.getView(android.R.id.button2));
        solo.goBack();
        solo.goBack();

        LoginHelper.loginToSettings(solo);

        solo.clickOnText("Nastavení filtru");
        assertFalse(solo.searchText("obd_speed"));

        solo.clickOnButton(0);
        solo.pressSpinnerItem(0, 1);
        solo.clearEditText(0);
        solo.enterText(0, "7.7");
        solo.clickOnView(solo.getView(R.id.dialog_filter_active_edit));
        solo.clickOnView(solo.getView(android.R.id.button3));

        solo.goBack();
        solo.goBack();

        LoginHelper.loginToSettings(solo);


        solo.clickOnText("Nastavení filtru");
        assertTrue(solo.searchText("obd_speed"));
        solo.clickOnText("obd_speed");
        assertTrue(solo.searchText("obd_speed"));
        assertTrue(solo.searchText("PERCENTAGE"));
        assertTrue(solo.searchText("7.7"));
        solo.isToggleButtonChecked(0);
        solo.clickOnView(solo.getView(android.R.id.button1));
    }

}
