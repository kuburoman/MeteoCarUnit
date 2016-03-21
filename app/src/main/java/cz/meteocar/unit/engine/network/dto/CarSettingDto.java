package cz.meteocar.unit.engine.network.dto;

/**
 * Created by Nell on 13.3.2016.
 */
public class CarSettingDto {

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
