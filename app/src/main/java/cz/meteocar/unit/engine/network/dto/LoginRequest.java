package cz.meteocar.unit.engine.network.dto;

/**
 * Created by Nell on 27.2.2016.
 */
public class LoginRequest {

    private String username;

    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
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
}
