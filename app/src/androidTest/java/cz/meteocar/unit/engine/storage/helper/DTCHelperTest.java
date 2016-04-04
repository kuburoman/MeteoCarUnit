package cz.meteocar.unit.engine.storage.helper;

import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.DTCEntity;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Nell on 30.3.2016.
 */
@RunWith(AndroidJUnit4.class)
public class DTCHelperTest {

    private DatabaseHelper db;
    private DTCHelper dao;

    private int saveId1;
    private int saveId2;

    @Before
    public void setUp() throws Exception {

        RenamingDelegatingContext context = new RenamingDelegatingContext(getTargetContext(), "test_");
        context.deleteDatabase("test_" + MySQLiteConfig.DATABASE_NAME);

        db = new DatabaseHelper(context);
        dao = new DTCHelper(db);

        DTCEntity input = new DTCEntity();
        input.setId(-1);
        input.setDtcCode("P0103");
        input.setTripId("trip");
        input.setTime(1L);
        input.setPosted(false);

        saveId1 = dao.save(input);
        assertNotEquals(-1, saveId1);

        input = new DTCEntity();
        input.setId(-1);
        input.setDtcCode("P0105");
        input.setTripId("trip");
        input.setTime(2L);
        input.setPosted(false);

        saveId2 = dao.save(input);
        assertNotEquals(-1, saveId2);
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void testSave() {
        DTCEntity input = new DTCEntity();
        input.setId(-1);
        input.setDtcCode("P0105");
        input.setTripId("trip2");
        input.setTime(10L);
        input.setPosted(false);

        int inputId = dao.save(input);

        DTCEntity result = dao.get(inputId);
        assertNotNull(result);

        assertEquals(input.getDtcCode(), result.getDtcCode());
        assertEquals(input.getTripId(), result.getTripId());
        assertEquals(input.getTime(), result.getTime());
        assertEquals(input.isPosted(), result.isPosted());
    }


    @Test
    public void testGetNumberOfRecords() {

        List<Integer> ids = new ArrayList<>();
        ids.add(saveId1);
        ids.add(saveId2);

        dao.updatePosted(ids, true);

        DTCEntity dtcEntity = dao.get(saveId1);
        assertNotNull(dtcEntity);
        assertEquals(saveId1, dtcEntity.getId());
        assertEquals(true, dtcEntity.isPosted());

        dtcEntity = dao.get(saveId2);
        assertNotNull(dtcEntity);
        assertEquals(saveId2, dtcEntity.getId());
        assertEquals(true, dtcEntity.isPosted());
    }
}
