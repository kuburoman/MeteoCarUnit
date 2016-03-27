package cz.meteocar.unit.engine.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import cz.meteocar.unit.engine.network.dto.LoginRequest;
import cz.meteocar.unit.engine.network.task.PostLoginTask;

/**
 * Created by Toms, 2014.
 */
public class NetworkService {

    private Context context;

    /**
     * Default constructor.
     *
     * @param ctx context of application
     */
    public NetworkService(Context ctx) {
        context = ctx;
    }

    public void loginUser(String username, String password) {
        new PostLoginTask().execute(new LoginRequest(username, password));
    }

    /**
     * Collects information about active network
     *
     * @return Return {@link NetworkInfo} or null.
     */
    private NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        return connManager.getActiveNetworkInfo();
    }


    public boolean isOnline() {
        NetworkInfo activeNetwork = getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

}
