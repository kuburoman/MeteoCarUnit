package cz.meteocar.unit.engine.convertor;

import org.junit.Test;

import cz.meteocar.unit.engine.obd.OBDMessage;
import cz.meteocar.unit.engine.obd.OBDService;
import cz.meteocar.unit.engine.storage.helper.filter.RecordVO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Nell on 23.1.2016.
 */
public class OBDEventPID2RecordVOConverterTest {

    OBDEventPID2RecordVOConverter converter = new OBDEventPID2RecordVOConverter();

    @Test
    public void testNull() {
        RecordVO result = converter.convert(null);

        assertNull(result);
    }

    @Test
    public void testCorrect() {
        OBDMessage obdMessage = new OBDMessage();
        obdMessage.setTag("tag");

        OBDService.OBDEventPID input = new OBDService.OBDEventPID(obdMessage, 1.0, null);
        input.setTripId("trip");
        input.setUserId("user");
        input.setTimeCreated(7L);

        RecordVO result = converter.convert(input);

        assertEquals(input.getTripId(), result.getTripId());
        assertEquals(input.getUserId(), result.getUserId());
        assertEquals(input.getMessage().getTag(), result.getType());
        assertEquals(input.getValue(), result.getValue(), 0);
        assertEquals(input.getTimeCreated(), result.getLastSaved());
        assertEquals(input.getTimeCreated(), result.getTime());
        assertEquals(false, result.isSaved());

    }
}