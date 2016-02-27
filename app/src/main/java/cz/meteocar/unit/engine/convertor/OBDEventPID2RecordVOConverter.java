package cz.meteocar.unit.engine.convertor;

import cz.meteocar.unit.engine.obd.event.OBDPidEvent;
import cz.meteocar.unit.engine.storage.helper.filter.RecordVO;

/**
 * Created by Nell on 23.1.2016.
 */
public class OBDEventPID2RecordVOConverter extends AbstractConverter<OBDPidEvent, RecordVO> {

    @Override
    protected RecordVO innerConvert(OBDPidEvent input) {
        RecordVO recordVO = new RecordVO();
        recordVO.setUserId(input.getUserId());
        recordVO.setTripId(input.getTripId());
        recordVO.setType(input.getMessage().getTag());
        recordVO.setTime(input.getTimeCreated());
        recordVO.setLastSaved(input.getTimeCreated());
        recordVO.setValue(input.getValue());
        recordVO.setSaved(false);
        return recordVO;
    }
}
