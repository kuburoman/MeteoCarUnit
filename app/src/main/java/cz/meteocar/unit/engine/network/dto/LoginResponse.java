package cz.meteocar.unit.engine.network.dto;

/**
 * Created by Nell on 27.2.2016.
 */
public class LoginResponse {

    private String status;
    private Boolean isAdmin;

    public LoginResponse(String status, Boolean isAdmin) {
        this.status = status;
        this.isAdmin = isAdmin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
