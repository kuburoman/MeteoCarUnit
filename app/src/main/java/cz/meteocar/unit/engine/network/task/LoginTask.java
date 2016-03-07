package cz.meteocar.unit.engine.network.task;

import android.content.Context;
import android.widget.Toast;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.network.dto.LoginRequest;
import cz.meteocar.unit.engine.network.dto.LoginResponse;
import cz.meteocar.unit.engine.network.event.LoginEvent;

/**
 * Created by Nell on 6.3.2016.
 */
public class LoginTask extends NetworkTask<LoginRequest, LoginResponse> {

    public LoginTask(Context context) {
        super(context);
    }

    @Override
    protected String getDataURL() {
        return "login";
    }

    @Override
    protected Class<LoginRequest> getClassIN() {
        return LoginRequest.class;
    }

    @Override
    protected Class<LoginResponse> getClassOUT() {
        return LoginResponse.class;
    }

    @Override
    protected LoginResponse doInBackground(LoginRequest... params) {
        return post(params[0]);
    }

    @Override
    protected void onPostExecute(LoginResponse loginResponse) {
        if (errorResponse != null) {
            Toast.makeText(context, errorResponse.getCode() + ": " + errorResponse.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        ServiceManager.getInstance().eventBus.post(new LoginEvent(loginResponse)).asynchronously();

    }
}
