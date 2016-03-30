package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.model.DTCEntity;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test for {@link DTCHelper}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DTCHelperTest {

    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_TIME = "time";
    private static final String COLUMN_NAME_USER_ID = "user_id";
    private static final String COLUMN_NAME_TRIP_ID = "trip_id";
    private static final String COLUMN_NAME_DTC_CODE = "dtc_code";
    private static final String COLUMN_NAME_POSTED = "posted";

    @Mock
    private ContentValues contentValues;

    @Spy
    @InjectMocks
    private DTCHelper helper = new DTCHelper(null);

    @Test
    public void testSave() throws DatabaseException {
        ContentValues contentValues = mock(ContentValues.class);
        doReturn(contentValues).when(helper).newContentValues();
        doReturn(0).when(helper).innerSave(any(Integer.class), any(ContentValues.class));

        DTCEntity obj = new DTCEntity();
        obj.setTime(1L);
        obj.setDtcCode("code");
        obj.setTripId("tripId");
        obj.setPosted(false);

        helper.save(obj);

        verify(contentValues).put(COLUMN_NAME_TIME, obj.getTime());
        verify(contentValues).put(COLUMN_NAME_DTC_CODE, obj.getDtcCode());
        verify(contentValues).put(COLUMN_NAME_TRIP_ID, obj.getTripId());
        verify(contentValues).put(COLUMN_NAME_POSTED, obj.isPosted());
    }

    @Test
    public void testConvert() {
        Cursor cursor = mock(Cursor.class);

        DTCEntity input = new DTCEntity();
        input.setId(1);
        input.setTime(0L);
        input.setTripId("setTripId");
        input.setDtcCode("setDtcCode");
        input.setPosted(false);

        doReturn(0).when(cursor).getColumnIndex(COLUMN_NAME_ID);
        doReturn(1).when(cursor).getColumnIndex(COLUMN_NAME_TIME);
        doReturn(2).when(cursor).getColumnIndex(COLUMN_NAME_TRIP_ID);
        doReturn(3).when(cursor).getColumnIndex(COLUMN_NAME_USER_ID);
        doReturn(4).when(cursor).getColumnIndex(COLUMN_NAME_DTC_CODE);
        doReturn(5).when(cursor).getColumnIndex(COLUMN_NAME_POSTED);

        doReturn(input.getId()).when(cursor).getInt(0);
        doReturn(input.getTime()).when(cursor).getLong(1);
        doReturn(input.getTripId()).when(cursor).getString(2);
        doReturn(input.getDtcCode()).when(cursor).getString(4);
        doReturn(0).when(cursor).getInt(5);

        DTCEntity result = helper.convert(cursor);

        assertEquals(input.getId(), result.getId());
        assertEquals(input.getTime(), result.getTime());
        assertEquals(input.getTripId(), result.getTripId());
        assertEquals(input.getDtcCode(), result.getDtcCode());
        assertEquals(input.isPosted(), result.isPosted());
    }

    @Test
    public void testBinaryToHex() {
        assertEquals("3", helper.binaryToHex("0011"));
    }

    @Test
    public void testHexToBinary() {
        assertEquals("0011", helper.hexToBinary("3"));
    }

    @Test
    public void testParseSingleCode() {
        assertEquals("P0103", helper.parseSingleCode("0103"));
    }

    @Test
    public void testParseFrame() {
        List<String> results = helper.parseFrame("010301050000");
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.contains("P0103"));
        assertTrue(results.contains("P0105"));
    }

    @Test
    public void testParseEveryFrame() {
        List<String> results = helper.parseEveryFrame("43 01 03 01 05 01 0643 01 10 00 00 00 00");
        assertNotNull(results);
        assertEquals(4, results.size());
        assertTrue(results.contains("P0103"));
        assertTrue(results.contains("P0105"));
        assertTrue(results.contains("P0106"));
        assertTrue(results.contains("P0110"));
    }


}