package cz.meteocar.unit.engine.network.event;

import cz.meteocar.unit.engine.ServiceManager;

/**
 * Created by Nell on 7.12.2015.
 */
public class LogUserRequestEvent extends ServiceManager.AppEvent{

    private String userId;
    private String username;
    private String password;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int getType() {
        return ServiceManager.AppEvent.EVENT_NETWORK;
    }
}
