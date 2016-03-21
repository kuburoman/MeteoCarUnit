package cz.meteocar.unit.engine.network.task.converter;

import com.google.common.base.Converter;

import cz.meteocar.unit.engine.network.dto.FilterSettingDto;
import cz.meteocar.unit.engine.storage.model.FilterSettingEntity;

/**
 * Converts between {@link FilterSettingEntity} and {@link FilterSettingDto}.
 */
public class FilterSettingsEntity2DtoConverter extends Converter<FilterSettingEntity, FilterSettingDto> {

    @Override
    protected FilterSettingDto doForward(FilterSettingEntity input) {
        FilterSettingDto result = new FilterSettingDto();
        result.setTag(input.getTag());
        result.setAlgorithm(input.getAlgorithm());
        result.setValue(input.getValue());
        result.setActive(input.isActive());
        result.setUpdateTime(input.getUpdateTime());
        return result;
    }

    @Override
    protected FilterSettingEntity doBackward(FilterSettingDto input) {
        FilterSettingEntity result = new FilterSettingEntity();
        result.setTag(input.getTag());
        result.setAlgorithm(input.getAlgorithm());
        result.setValue(input.getValue());
        result.setActive(input.isActive());
        result.setUpdateTime(input.getUpdateTime());
        return result;
    }
}
