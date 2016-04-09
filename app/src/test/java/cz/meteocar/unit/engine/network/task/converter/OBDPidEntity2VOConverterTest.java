package cz.meteocar.unit.engine.network.task.converter;

import org.junit.Test;

import cz.meteocar.unit.engine.network.dto.OBDPidDto;
import cz.meteocar.unit.engine.storage.model.ObdPidEntity;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link OBDPidEntity2DtoConverter}.
 */
public class OBDPidEntity2VOConverterTest {

    private OBDPidEntity2DtoConverter converter = new OBDPidEntity2DtoConverter();


    @Test
    public void testForward() {
        ObdPidEntity input = new ObdPidEntity();
        input.setName("setName");
        input.setTag("setTag");
        input.setFormula("setFormula");
        input.setMin(0);
        input.setMax(10);
        input.setActive(true);
        input.setPidCode("setPidCode");
        input.setUpdateTime(1L);

        OBDPidDto result = converter.doForward(input);
        assertEquals(input.getName(), result.getName());
        assertEquals(input.getTag(), result.getTag());
        assertEquals(input.getFormula(), result.getFormula());
        assertEquals(input.getMin(), result.getMin());
        assertEquals(input.getMax(), result.getMax());
        assertEquals(true, result.isActive());
        assertEquals(input.getPidCode(), result.getPidCode());
        assertEquals(input.getUpdateTime(), result.getUpdateTime());
    }


    @Test
    public void testBackward() {
        OBDPidDto input = new OBDPidDto();
        input.setName("setName");
        input.setTag("setTag");
        input.setFormula("setFormula");
        input.setMin(0);
        input.setMax(10);
        input.setActive(true);
        input.setPidCode("setPidCode");
        input.setUpdateTime(1L);

        ObdPidEntity result = converter.doBackward(input);
        assertEquals(input.getName(), result.getName());
        assertEquals(input.getTag(), result.getTag());
        assertEquals(input.getFormula(), result.getFormula());
        assertEquals(input.getMin(), result.getMin());
        assertEquals(input.getMax(), result.getMax());
        assertEquals(true, result.isActive());
        assertEquals(input.getPidCode(), result.getPidCode());
        assertEquals(input.getUpdateTime(), result.getUpdateTime());

    }

}