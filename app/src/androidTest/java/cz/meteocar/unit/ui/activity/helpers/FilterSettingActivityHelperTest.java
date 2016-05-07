package cz.meteocar.unit.ui.activity.helpers;

import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

import cz.meteocar.unit.R;
import cz.meteocar.unit.ui.activity.*;

/**
 * Filter settings dialog test.
 */
public class FilterSettingActivityHelperTest extends AbstractSettingActivityHelper {

    public void testDelete() throws InterruptedException {
        solo.unlockScreen();

        loginToSettings();

        solo.clickInList(2);
        solo.clickOnText("obd_speed");
        solo.clickOnView(solo.getView(BUTTON_NEGATIVE));

        assertFalse(solo.searchText("obd_speed"));
    }

    public void testAdd() throws Exception {
        testDelete();

        solo.clickOnButton(0);
        solo.pressSpinnerItem(0, 0);
        solo.pressSpinnerItem(1, 0);
        solo.clearEditText(0);
        solo.enterText(0, "1.0");
        solo.clickOnView(solo.getView(R.id.dialog_filter_active_edit));
        solo.clickOnView(solo.getView(BUTTON_POSITIVE));

        assertTrue(solo.searchText("obd_speed"));
        solo.clickOnText("obd_speed");

        Spinner filterAlgorithm = (Spinner) solo.getView(R.id.dialog_filter_algorithm_edit);
        assertEquals(0, filterAlgorithm.getSelectedItemPosition());

        Spinner filterTag = (Spinner) solo.getView(R.id.dialog_filter_tag_edit);
        assertEquals(0, filterTag.getSelectedItemPosition());

        EditText value = (EditText) solo.getView(R.id.dialog_filter_value_edit);
        assertEquals("1.0", value.getText().toString());

        ToggleButton toggleButton = (ToggleButton) solo.getView(R.id.dialog_filter_active_edit);
        assertTrue(toggleButton.isChecked());
    }

    public void testUpdate() throws Exception {
        solo.unlockScreen();

        loginToSettings();

        solo.clickInList(2);
        solo.clickOnText("obd_speed");

        solo.pressSpinnerItem(0, 1);
        solo.clearEditText(0);
        solo.enterText(0, "7.7");
        solo.clickOnView(solo.getView(R.id.dialog_filter_active_edit));
        solo.clickOnView(solo.getView(BUTTON_POSITIVE));

        assertTrue(solo.searchText("obd_speed"));
        solo.clickOnText("obd_speed");

        Spinner filterAlgorithm = (Spinner) solo.getView(R.id.dialog_filter_algorithm_edit);
        assertEquals(1, filterAlgorithm.getSelectedItemPosition());

        Spinner filterTag = (Spinner) solo.getView(R.id.dialog_filter_tag_edit);
        assertEquals(0, filterTag.getSelectedItemPosition());

        EditText value = (EditText) solo.getView(R.id.dialog_filter_value_edit);
        assertEquals("7.7", value.getText().toString());

        ToggleButton toggleButton = (ToggleButton) solo.getView(R.id.dialog_filter_active_edit);
        assertFalse(toggleButton.isChecked());
    }


}