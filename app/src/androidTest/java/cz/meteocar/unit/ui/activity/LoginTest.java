package cz.meteocar.unit.ui.activity;

import android.graphics.Point;
import android.widget.EditText;

import cz.meteocar.unit.R;

/**
 * Created by Nell on 28.4.2016.
 */
public class LoginTest extends AbstractSettingActivityHelper {

    public void testLoginToApplication() throws InterruptedException {
        setWifiEnabled(false);

        solo.unlockScreen();

        solo.enterText((EditText) solo.getView(R.id.nameEditText), "Johny");
        solo.enterText((EditText) solo.getView(R.id.pwdEditText), "root");

        solo.clickOnView(solo.getView(R.id.btnlogin));

        solo.waitForFragmentByTag("dashboardFragment");

        synchronized (solo) {
            solo.wait(5000);
        }

        openNavigationDrawer();

        synchronized (solo) {
            solo.wait(2000);
        }

        solo.clickInList(4);

        solo.waitForActivity(LoginActivity.class);

        setWifiEnabled(true);
    }

    public void openNavigationDrawer() {
        Point deviceSize = new Point();
        solo.getCurrentActivity().getWindowManager().getDefaultDisplay().getSize(deviceSize);

        int screenWidth = deviceSize.x;
        int screenHeight = deviceSize.y;
        int fromX = 20;
        int toX = screenWidth / 2;
        int fromY = screenHeight / 2;
        int toY = fromY;

        solo.drag(fromX, toX, fromY, toY, 1);
    }

}
