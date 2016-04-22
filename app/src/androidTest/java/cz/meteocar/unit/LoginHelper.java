package cz.meteocar.unit;

import android.widget.EditText;

import com.robotium.solo.Solo;

/**
 * Created by Nell on 22.4.2016.
 */
public class LoginHelper {

    public static void loginToSettings(Solo solo){
        solo.enterText((EditText) solo.getView(R.id.nameEditText), "root");
        solo.enterText((EditText) solo.getView(R.id.pwdEditText), "root");
        solo.clickOnCheckBox(0);

        solo.clickOnView(solo.getView(R.id.btnlogin));
    }
}
