package cz.meteocar.unit.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;

import com.robotium.solo.Solo;

import cz.meteocar.unit.BuildConfig;
import cz.meteocar.unit.LoginHelper;
import cz.meteocar.unit.R;

/**
 * Created by Nell on 20.4.2016.
 */
public class AbstractSettingActivityHelper extends ActivityInstrumentationTestCase2<SplashActivity> {

    public static final int BUTTON_POSITIVE = android.R.id.button1;

    /**
     * The identifier for the negative button.
     */
    public static final int BUTTON_NEGATIVE = android.R.id.button2;

    /**
     * The identifier for the neutral button.
     */
    public static final int BUTTON_NEUTRAL = android.R.id.button3;

    protected Solo solo;

    public AbstractSettingActivityHelper() {
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

    public void loginToSettings(){
        solo.enterText((EditText) solo.getView(R.id.nameEditText), "root");
        solo.enterText((EditText) solo.getView(R.id.pwdEditText), "root");
        solo.clickOnCheckBox(0);

        solo.clickOnView(solo.getView(R.id.btnlogin));
    }
}
