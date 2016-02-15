package cz.meteocar.unit.engine.storage.helper.filter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import cz.meteocar.unit.engine.convertor.RecordVO2EntityConverter;
import cz.meteocar.unit.engine.storage.helper.FilterSettingHelper;
import cz.meteocar.unit.engine.storage.helper.RecordHelper;
import cz.meteocar.unit.engine.storage.model.FilterSettingEntity;


public class Filter {


    protected HashMap<String, RecordVO> records;

    protected RecordVO2EntityConverter recordVO2EntityConverter = new RecordVO2EntityConverter();

    protected FilterSettingHelper filterSettingHelper;
    protected RecordHelper recordHelper;

    public Filter(FilterSettingHelper filterSettingHelper, RecordHelper recordHelper) {
        records = new HashMap<>();
        this.filterSettingHelper = filterSettingHelper;
        this.recordHelper = recordHelper;
    }

    public void process(RecordVO evt) {

        RecordVO curr = evt;

        RecordVO prev = records.get(curr.getType());

        if (prev == null) {
            saveNew(curr);
            return;
        }

        if (prev.getUserId() != curr.getUserId() || prev.getTripId() != curr.getTripId()) {
            saveNew(curr);
            return;
        }

        FilterSettingEntity setting = getFilter(curr.getType());

        if (setting == null || !setting.isActive()) {
            saveNew(curr);
            return;
        }

        if (Math.abs(prev.getValue() - curr.getValue()) >= (prev.getValue() * setting.getReduceValue() * 0.01 * (setting.getReduceType() == ReducerType.PERCENTAGE ? 1 : 0))
                + (setting.getReduceValue() * (setting.getReduceType() == ReducerType.ABSOLUTE ? 1 : 0))) {

            if (!prev.isSaved()) {
                if (setting.isRounding()) {
                    prev.setValue(round(prev.getValue(), setting.getRoundingDecimal()));
                }

                saveIntoDB(prev);
            }

            saveNew(curr);
            return;
        }

        if (curr.getLastSaved() - prev.getLastSaved() > setting.getMaxTime()) {
            if (setting.isRounding()) {
                prev.setValue(round(prev.getValue(), setting.getRoundingDecimal()));
            }
            if (!prev.isSaved()) {
                saveIntoDB(prev);
            }
            curr.setLastSaved(prev.getTime());
            records.put(curr.getType(), curr);
            return;
        }

        if (!prev.isSaved() && setting.isRounding()) {
            long currTime = curr.getTime() - prev.getTime();
            long prevTime = prev.getTime() - prev.getLastSaved();
            curr.setValue((((prev.getValue() * prevTime) + (curr.getValue() * currTime)) / (currTime + prevTime)));
            curr.setLastSaved(prev.getLastSaved());
        }

        records.put(curr.getType(), curr);

    }

    protected void saveNew(RecordVO vo) {
        vo.setSaved(true);
        vo.setLastSaved(vo.getTime());
        records.put(vo.getType(), vo);

        saveIntoDB(vo);
    }

    protected void saveIntoDB(RecordVO vo) {
        recordHelper.save(recordVO2EntityConverter.convert(vo));
    }

    protected FilterSettingEntity getFilter(String obdCode) {
        return filterSettingHelper.getByCode(obdCode);
    }

    protected double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
