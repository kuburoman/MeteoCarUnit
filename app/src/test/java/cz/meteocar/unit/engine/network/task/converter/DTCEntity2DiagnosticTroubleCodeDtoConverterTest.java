package cz.meteocar.unit.engine.network.task.converter;

import org.junit.Test;

import cz.meteocar.unit.engine.network.dto.DiagnosticTroubleCodeDto;
import cz.meteocar.unit.engine.storage.model.DTCEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for {@link DTCEntity2DiagnosticTroubleCodeDtoConverter}.
 */
public class DTCEntity2DiagnosticTroubleCodeDtoConverterTest {

    private DTCEntity2DiagnosticTroubleCodeDtoConverter converter = new DTCEntity2DiagnosticTroubleCodeDtoConverter();

    @Test
    public void testForward() {
        DTCEntity input = new DTCEntity();
        input.setDtcCode("dtcCode");
        input.setTime(1L);
        input.setTripId("trip");

        DiagnosticTroubleCodeDto result = converter.convert(input);
        assertNotNull(result);
        assertEquals(input.getDtcCode(), result.getCode());
        assertEquals(input.getTime(), result.getTime());
        assertEquals(input.getTripId(), result.getTripHashcode());

    }
}