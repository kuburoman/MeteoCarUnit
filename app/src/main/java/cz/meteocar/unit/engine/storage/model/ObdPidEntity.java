package cz.meteocar.unit.engine.storage.model;

/**
 * OBD pid entity.
 */
public class ObdPidEntity extends AbstractEntity {

    private String name;
    private String tag;
    private String pidCode;
    private String formula;
    private int min;
    private int max;
    private boolean active;
    private Long updateTime;

    public String getName() {
        return name;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPidCode() {
        return pidCode;
    }

    public void setPidCode(String pidCode) {
        this.pidCode = pidCode;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}
