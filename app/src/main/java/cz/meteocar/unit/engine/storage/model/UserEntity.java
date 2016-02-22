package cz.meteocar.unit.engine.storage.model;

/**
 * User entity
 */
public class UserEntity extends AbstractEntity {

    private String username;
    private String password;
    private Boolean logged;

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

    public Boolean getLogged() {
        return logged;
    }

    public void setLogged(Boolean logged) {
        this.logged = logged;
    }
}
