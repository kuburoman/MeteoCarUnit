package cz.meteocar.unit.ui.activity.helpers;

import cz.meteocar.unit.R;
import cz.meteocar.unit.ui.activity.*;

/**
 * Created by Nell on 22.4.2016.
 */
public class FilterSettingActivityHelperTest extends AbstractSettingActivityHelper {

    public void testDelete(){
        solo.unlockScreen();

        loginToSettings();

        solo.clickOnText("Nastavení filtru");
        solo.clickOnText("obd_speed");
        solo.clickOnView(solo.getView(BUTTON_NEGATIVE));
        solo.goBack();
        solo.goBack();

        loginToSettings();

        solo.clickOnText("Nastavení filtru");
        assertFalse(solo.searchText("obd_speed"));
    }

    public void testAdd() throws Exception {
        testDelete();

        solo.clickOnButton(0);
        solo.pressSpinnerItem(0, 1);
        solo.clearEditText(0);
        solo.enterText(0, "7.7");
        solo.clickOnView(solo.getView(R.id.dialog_filter_active_edit));
        solo.clickOnView(solo.getView(BUTTON_POSITIVE));

        solo.goBack();
        solo.goBack();

        loginToSettings();

        solo.clickOnText("Nastavení filtru");
        assertTrue(solo.searchText("obd_speed"));
        solo.clickOnText("obd_speed");
        assertTrue(solo.searchText("obd_speed"));
        assertTrue(solo.searchText("PERCENTAGE"));
        assertTrue(solo.searchText("7.7"));
        assertTrue(solo.isToggleButtonChecked(0));
    }

    public void testUpdate() throws Exception {

        solo.unlockScreen();

        loginToSettings();

        solo.clickOnText("Nastavení filtru");
        solo.clickOnText("obd_speed");

        solo.clickOnButton(0);
        solo.pressSpinnerItem(0, 1);
        solo.clearEditText(0);
        solo.enterText(0, "7.7");
        solo.clickOnView(solo.getView(R.id.dialog_filter_active_edit));
        solo.clickOnView(solo.getView(BUTTON_POSITIVE));

        solo.goBack();
        solo.goBack();

        loginToSettings();

        solo.clickOnText("Nastavení filtru");
        assertTrue(solo.searchText("obd_speed"));
        solo.clickOnText("obd_speed");
        assertTrue(solo.searchText("obd_speed"));
        assertTrue(solo.searchText("PERCENTAGE"));
        assertTrue(solo.searchText("7.7"));
        assertTrue(solo.isToggleButtonChecked(0));
    }


}