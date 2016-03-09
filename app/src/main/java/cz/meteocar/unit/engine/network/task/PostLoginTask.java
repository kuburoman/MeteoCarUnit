package cz.meteocar.unit.engine.network.task;

import android.os.AsyncTask;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.event.ErrorViewType;
import cz.meteocar.unit.engine.event.NetworkErrorEvent;
import cz.meteocar.unit.engine.network.NetworkException;
import cz.meteocar.unit.engine.network.dto.LoginRequest;
import cz.meteocar.unit.engine.network.dto.LoginResponse;
import cz.meteocar.unit.engine.network.event.LoginEvent;

/**
 * Created by Nell on 6.3.2016.
 */
public class PostLoginTask extends AsyncTask<LoginRequest, Void, Void> {

    NetworkConnector<LoginRequest, LoginResponse> networkConnector;

    public PostLoginTask() {
        networkConnector = new NetworkConnector<>(LoginRequest.class, LoginResponse.class, "login");
    }

    @Override
    protected Void doInBackground(LoginRequest... params) {
        try {
            LoginResponse response = networkConnector.post(params[0]);
            ServiceManager.getInstance().eventBus.post(new LoginEvent(response)).asynchronously();
        } catch (NetworkException e) {
            ServiceManager.getInstance().eventBus.post(new NetworkErrorEvent(e.getErrorResponse(), ErrorViewType.LOGIN)).asynchronously();
        }
        return null;
    }
}
