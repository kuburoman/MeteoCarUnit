package cz.meteocar.unit.ui.activity.helpers;

import cz.meteocar.unit.ui.activity.AbstractSettingActivityHelper;

import static org.junit.Assert.*;

/**
 * Created by Nell on 22.4.2016.
 */
public class CarSettingActivityHelperTest extends AbstractSettingActivityHelper {

    private String settingName = "RECORD_CONVERT_PERIOD";

    public void testAdd(){
        solo.unlockScreen();
        loginToSettings();

        solo.clickInList(3);
        solo.clickOnButton(0);

        solo.pressSpinnerItem(0, 1);
        solo.enterText(0, "10");
        solo.clickOnView(solo.getView(BUTTON_POSITIVE));
        solo.goBack();
        solo.goBack();

        loginToSettings();
        solo.clickInList(3);
        assertTrue(solo.searchText(settingName));
        solo.clickOnText(settingName);
        assertTrue(solo.searchText(settingName));
        assertTrue(solo.searchText("10"));
    }

    public void testDelete(){
        testAdd();
        solo.clickOnView(solo.getView(BUTTON_NEGATIVE));

        solo.goBack();
        solo.goBack();

        loginToSettings();

        solo.clickInList(3);
        assertFalse(solo.searchText(settingName));
    }

    public void testUpdate(){
        testAdd();
        solo.goBack();
        solo.clickOnText(settingName);
        solo.clearEditText(0);
        solo.enterText(0, "7");
        solo.clickOnView(solo.getView(BUTTON_POSITIVE));
        solo.goBack();
        solo.goBack();

        loginToSettings();
        solo.clickInList(3);
        assertTrue(solo.searchText(settingName));
        solo.clickOnText(settingName);
        assertTrue(solo.searchText(settingName));
        assertTrue(solo.searchText("7"));
    }

}