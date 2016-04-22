package cz.meteocar.unit.ui.activity.helpers;

import android.widget.EditText;

import cz.meteocar.unit.R;
import cz.meteocar.unit.ui.activity.AbstractSettingActivityHelper;

import static org.junit.Assert.*;

/**
 * Created by Nell on 22.4.2016.
 */
public class BoardUnitSettingActivityHelperTest extends AbstractSettingActivityHelper {

    public void testSetUp(){
        solo.unlockScreen();
        loginToSettings();
        solo.clickOnText("Nastavení palubní jednotky");
        solo.clickOnCheckBox(0);
        solo.clickOnText("Nastavení palubní jednotky");
        solo.clearEditText(0);
        solo.enterText(0, "name");
        solo.clearEditText(1);
        solo.enterText(1, "password");
        solo.clickOnView(solo.getView(BUTTON_POSITIVE));
        solo.clickOnView(solo.getView(BUTTON_POSITIVE));
        solo.goBack();
        solo.goBack();

        solo.enterText((EditText) solo.getView(R.id.nameEditText), "name");
        solo.enterText((EditText) solo.getView(R.id.pwdEditText), "password");
        solo.clickOnCheckBox(0);
        solo.clickOnView(solo.getView(R.id.btnlogin));
        solo.clickOnText("Nastavení palubní jednotky");
        assertTrue(solo.isCheckBoxChecked(0));
        solo.clickOnText("Nastavení palubní jednotky");
        assertTrue(solo.searchText("name"));
        assertTrue(solo.searchText("password"));
    }

}