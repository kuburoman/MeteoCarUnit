package cz.meteocar.unit.ui.activity.helpers;

import cz.meteocar.unit.ui.activity.AbstractSettingActivityHelper;

/**
 * Created by Nell on 22.4.2016.
 */
public class ObdPidSettingActivityHelperTest extends AbstractSettingActivityHelper {

        private String itemName = "OBD Nastavení";
        private String itemSubName = "OBD PID P5";

        public void testAdd(){
            solo.unlockScreen();

            loginToSettings();

            solo.clickInList(0);
            solo.clickInList(2);

            solo.clickOnButton(0);

            solo.enterText(0, "test");
            solo.enterText(1, "test_test");
            solo.enterText(2, "0100");
            solo.enterText(3, "A");
            solo.enterText(4, "0");
            solo.enterText(5, "100");
            solo.clickOnCheckBox(0);
            solo.clickOnView(solo.getView(BUTTON_POSITIVE));



        }
}