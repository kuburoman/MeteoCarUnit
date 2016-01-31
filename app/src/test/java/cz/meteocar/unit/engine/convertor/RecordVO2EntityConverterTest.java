package cz.meteocar.unit.engine.convertor;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import cz.meteocar.unit.engine.obd.OBDMessage;
import cz.meteocar.unit.engine.obd.OBDService;
import cz.meteocar.unit.engine.storage.helper.filter.RecordVO;
import cz.meteocar.unit.engine.storage.model.RecordEntity;

import static org.junit.Assert.*;

/**
 * Created by Nell on 24.1.2016.
 */
public class RecordVO2EntityConverterTest {

    RecordVO2EntityConverter converter = new RecordVO2EntityConverter();

    @Test
    public void testNull() {
        RecordEntity result = converter.convert(null);

        assertNull(result);
    }

    @Test
    public void testCorrect() throws JSONException {

        RecordVO input = new RecordVO();
        input.setUserId("user");
        input.setTripId("trip");
        input.setValue(2.2);
        input.setTime(200L);
        input.setType("type");

        RecordEntity result = converter.convert(input);

        assertEquals(input.getTripId(), result.getTripId());
        assertEquals(input.getUserId(), result.getUserName());
        assertEquals(input.getType(), result.getType());
        assertEquals(input.getTime(), result.getTime());

        JSONObject jsonObject = new JSONObject(result.getJson());
        assertEquals(input.getValue(), jsonObject.getDouble("value"), 0);

    }

}