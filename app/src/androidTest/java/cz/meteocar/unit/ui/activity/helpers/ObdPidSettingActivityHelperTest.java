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


        EditText max = (EditText) solo.getView(R.id.dialog_obd_max_edit);
        EditText min = (EditText) solo.getView(R.id.dialog_obd_min_edit);

        solo.clearEditText(min);
        solo.clearEditText(max);

        solo.clickOnView(solo.getView(BUTTON_POSITIVE));

        EditText name = (EditText) solo.getView(R.id.dialog_obd_name_edit);

        EditText code = (EditText) solo.getView(R.id.dialog_obd_code_edit);
        EditText tag = (EditText) solo.getView(R.id.dialog_obd_tag_edit);
        EditText formula = (EditText) solo.getView(R.id.dialog_obd_formula_edit);
        max = (EditText) solo.getView(R.id.dialog_obd_max_edit);
        min = (EditText) solo.getView(R.id.dialog_obd_min_edit);

        assertNotNull(name.getError());
        assertNotNull(code.getError());
        assertNotNull(tag.getError());
        assertNotNull(formula.getError());
        assertNotNull(min.getError());
        assertNotNull(max.getError());

        solo.enterText(name, "test");
        solo.enterText(tag, "obd_speed");
        solo.enterText(code, "0100");
        solo.enterText(formula, "A");
        solo.enterText(min, "0");
        solo.enterText(max, "100");
        solo.clickOnCheckBox(0);
        solo.clickOnView(solo.getView(BUTTON_POSITIVE));

        assertNotNull(tag.getError());

        solo.clearEditText(2);
        solo.enterText(tag, "test_test");

        solo.clickOnView(solo.getView(BUTTON_POSITIVE));

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