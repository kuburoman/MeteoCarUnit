package cz.meteocar.unit.engine.network.task.converter;

import com.google.common.base.Converter;

import cz.meteocar.unit.engine.network.dto.OBDPidDto;
import cz.meteocar.unit.engine.storage.model.ObdPidEntity;

/**
 * Converts between {@link ObdPidEntity} and {@link OBDPidDto}
 */
public class OBDPidEntity2DtoConverter extends Converter<ObdPidEntity, OBDPidDto> {

    @Override
    protected OBDPidDto doForward(ObdPidEntity input) {
        OBDPidDto result = new OBDPidDto();
        result.setName(input.getName());
        result.setTag(input.getTag());
        result.setFormula(input.getFormula());
        result.setMin(input.getMin());
        result.setMax(input.getMax());
        result.setActive(input.isActive());
        result.setPidCode(input.getPidCode());
        result.setUpdateTime(input.getUpdateTime());
        return result;
    }

    @Override
    protected ObdPidEntity doBackward(OBDPidDto input) {
        ObdPidEntity result = new ObdPidEntity();
        result.setName(input.getName());
        result.setTag(input.getTag());
        result.setFormula(input.getFormula());
        result.setMin(input.getMin());
        result.setMax(input.getMax());
        result.setActive(input.isActive());
        result.setPidCode(input.getPidCode());
        result.setUpdateTime(input.getUpdateTime());
        return result;
    }
}
