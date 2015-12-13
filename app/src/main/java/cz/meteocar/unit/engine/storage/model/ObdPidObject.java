package cz.meteocar.unit.engine.storage.model;

/**
 * Created by Toms, 2014.
 */
public class ObdPidObject {

    public ObdPidObject() {
    }

    public ObdPidObject(int id, String name, String tag, String pidCode, String formula, int min, int max, int active, int locked) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.pidCode = pidCode;
        this.formula = formula;
        this.min = min;
        this.max = max;
        this.active = active;
        this.locked = locked;
    }

    public int id = -1;
    public String name;
    public String tag;
    public String pidCode;
    public String formula;
    public int min;
    public int max;
    public int active;
    public int locked;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public int getLocked() {
        return locked;
    }

    public void setLocked(int locked) {
        this.locked = locked;
    }

}
