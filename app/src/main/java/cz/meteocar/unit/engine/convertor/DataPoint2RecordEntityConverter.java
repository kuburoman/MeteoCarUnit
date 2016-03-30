package cz.meteocar.unit.engine.convertor;

import cz.meteocar.unit.engine.storage.model.RecordEntity;
import cz.meteocar.unit.engine.storage.simplify.DataPoint;

/**
 * Converts from {@link DataPoint} to {@link RecordEntity}.
 */
public class DataPoint2RecordEntityConverter extends AbstractConverter<DataPoint, RecordEntity> {

    @Override
    protected RecordEntity innerConvert(DataPoint input) {
        return input.getRecordEntity();
    }
}
