package cz.meteocar.unit.engine.storage.model;

/**
 * Created by Nell on 21.3.2016.
 */
public class CarSettingEntity extends AbstractEntity {

    private String code;
    private String value;
    private Long updateTime;
    private boolean active;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
