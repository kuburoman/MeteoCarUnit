package cz.meteocar.unit.engine.network.task.converter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import cz.meteocar.unit.engine.network.dto.FilterSettingDto;
import cz.meteocar.unit.engine.storage.model.FilterSettingEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for {@link FilterSettingsEntity2DtoConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FilterSettingsEntity2VOConverterTest {

    private FilterSettingsEntity2DtoConverter converter = new FilterSettingsEntity2DtoConverter();

    @Test
    public void testForward() {
        FilterSettingEntity input = new FilterSettingEntity();
        input.setTag("setTag");
        input.setAlgorithm("setAlgorithm");
        input.setValue(2.0);
        input.setActive(true);
        input.setUpdateTime(1L);

        FilterSettingDto result = converter.doForward(input);
        assertNotNull(result);
        assertEquals(input.getTag(), result.getTag());
        assertEquals(input.getAlgorithm(), result.getAlgorithm());
        assertEquals(input.isActive(), result.isActive());
        assertEquals(input.getValue(), result.getValue());
        assertEquals(input.getUpdateTime(), result.getUpdateTime());
    }


    @Test
    public void testBackward() {
        FilterSettingDto input = new FilterSettingDto();
        input.setTag("setTag");
        input.setAlgorithm("setAlgorithm");
        input.setValue(2.0);
        input.setActive(true);
        input.setUpdateTime(1L);

        FilterSettingEntity result = converter.doBackward(input);
        assertNotNull(result);
        assertEquals(input.getTag(), result.getTag());
        assertEquals(input.getAlgorithm(), result.getAlgorithm());
        assertEquals(input.isActive(), result.isActive());
        assertEquals(input.getValue(), result.getValue());
        assertEquals(input.getUpdateTime(), result.getUpdateTime());
    }
}