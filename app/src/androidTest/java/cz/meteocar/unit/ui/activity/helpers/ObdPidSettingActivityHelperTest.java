package cz.meteocar.unit.ui.activity.helpers;

import android.widget.EditText;

import cz.meteocar.unit.R;
import cz.meteocar.unit.ui.activity.AbstractSettingActivityHelper;

/**
 * Created by Nell on 22.4.2016.
 */
public class ObdPidSettingActivityHelperTest extends AbstractSettingActivityHelper {

    public void testAdd() {
        solo.unlockScreen();

        loginToSettings();

        solo.clickInList(0);
        solo.clickInList(3);

        solo.clickOnButton(0);
        solo.clearEditText(4);
        solo.clearEditText(5);

        solo.clickOnView(solo.getView(BUTTON_POSITIVE));

        EditText name = (EditText) solo.getView(R.id.dialog_obd_name_edit);
        EditText max = (EditText) solo.getView(R.id.dialog_obd_max_edit);
        EditText code = (EditText) solo.getView(R.id.dialog_obd_code_edit);
        EditText tag = (EditText) solo.getView(R.id.dialog_obd_tag_edit);
        EditText formula = (EditText) solo.getView(R.id.dialog_obd_formula_edit);
        EditText min = (EditText) solo.getView(R.id.dialog_obd_min_edit);
        assertNotNull(name.getError());
        assertNotNull(code.getError());
        assertNotNull(tag.getError());
        assertNotNull(formula.getError());
        assertNotNull(min.getError());
        assertNotNull(max.getError());

        solo.enterText(0, "test");
        solo.enterText(2, "obd_speed");
        solo.enterText(1, "0100");
        solo.enterText(3, "A");
        solo.enterText(4, "0");
        solo.enterText(5, "100");
        solo.clickOnCheckBox(0);
        solo.clickOnView(solo.getView(BUTTON_POSITIVE));

        assertNotNull(tag.getError());

        solo.clearEditText(2);
        solo.enterText(2, "test_test");

        solo.clickOnView(solo.getView(BUTTON_POSITIVE));

        assertTrue(solo.searchText("test"));

        solo.goBack();
        solo.goBack();
        solo.goBack();

        loginToSettings();

        solo.clickInList(0);
        solo.clickInList(3);

        assertTrue(solo.searchText("test"));
        solo.clickOnText("test");

        assertTrue(solo.searchText("test"));
        assertTrue(solo.searchText("test_test"));
        assertTrue(solo.searchText("0100"));
        assertTrue(solo.searchText("A"));
        assertTrue(solo.searchText("0"));
        assertTrue(solo.searchText("100"));
    }

    public void testDelete(){
        testAdd();
        solo.clickOnView(solo.getView(BUTTON_NEGATIVE));

        assertFalse(solo.searchText("test"));
    }
}