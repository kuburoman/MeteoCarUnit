package cz.meteocar.unit.engine.network.task.converter;

import com.google.common.base.Converter;

import cz.meteocar.unit.engine.network.dto.CarSettingDto;
import cz.meteocar.unit.engine.storage.model.CarSettingEntity;

/**
 * Converts between {@link CarSettingEntity} and {@link CarSettingDto}.
 */
public class CarSettingsEntity2DtoConverter extends Converter<CarSettingEntity, CarSettingDto> {

    @Override
    protected CarSettingDto doForward(CarSettingEntity input) {
        CarSettingDto result = new CarSettingDto();
        result.setCode(input.getCode());
        result.setValue(input.getValue());
        result.setActive(input.isActive());
        result.setUpdateTime(input.getUpdateTime());
        return result;
    }

    @Override
    protected CarSettingEntity doBackward(CarSettingDto input) {
        CarSettingEntity result = new CarSettingEntity();
        result.setCode(input.getCode());
        result.setValue(input.getValue());
        result.setActive(input.isActive());
        result.setUpdateTime(input.getUpdateTime());
        return result;
    }
}
