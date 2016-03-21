package cz.meteocar.unit.engine.network.task.converter;

import org.junit.Test;

import cz.meteocar.unit.engine.network.dto.CarSettingDto;
import cz.meteocar.unit.engine.storage.model.CarSettingEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for {@link CarSettingsEntity2DtoConverter}.
 */
public class CarSettingsEntity2DtoConverterTest {

    private CarSettingsEntity2DtoConverter converter = new CarSettingsEntity2DtoConverter();

    @Test
    public void testForward() {
        CarSettingEntity input = new CarSettingEntity();
        input.setCode("setCode");
        input.setValue("setValue");
        input.setActive(true);
        input.setUpdateTime(1L);

        CarSettingDto result = converter.doForward(input);
        assertNotNull(result);
        assertEquals(input.getCode(), result.getCode());
        assertEquals(input.isActive(), result.isActive());
        assertEquals(input.getValue(), result.getValue());
        assertEquals(input.getUpdateTime(), result.getUpdateTime());
    }

    @Test
    public void testBackward() {
        CarSettingDto input = new CarSettingDto();
        input.setCode("setCode");
        input.setValue("setValue");
        input.setActive(true);
        input.setUpdateTime(1L);

        CarSettingEntity result = converter.doBackward(input);
        assertNotNull(result);
        assertEquals(input.getCode(), result.getCode());
        assertEquals(input.isActive(), result.isActive());
        assertEquals(input.getValue(), result.getValue());
        assertEquals(input.getUpdateTime(), result.getUpdateTime());
    }

}