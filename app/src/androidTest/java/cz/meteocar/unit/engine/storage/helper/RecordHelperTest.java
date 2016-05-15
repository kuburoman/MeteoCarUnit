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
import cz.meteocar.unit.engine.storage.model.RecordEntity;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for {@link RecordHelper}.
 */
@RunWith(AndroidJUnit4.class)
public class RecordHelperTest {

    public DatabaseHelper db;
    public RecordHelper dao;


    @Before
    public void setUp() throws Exception {

        RenamingDelegatingContext context = new RenamingDelegatingContext(getTargetContext(), "test_");
        context.deleteDatabase("test_" + MySQLiteConfig.DATABASE_NAME);

        db = new DatabaseHelper(context);
        dao = new RecordHelper(db);

        RecordEntity input = new RecordEntity();
        input.setId(-1);
        input.setType("type");
        input.setTripId("trip");
        input.setUserName("user");
        input.setTime(1L);
        input.setProcessed(false);

        int saveId = dao.save(input);
        assertNotEquals(-1, saveId);

        saveId = dao.save(input);
        assertNotEquals(-1, saveId);


        input = new RecordEntity();
        input.setId(-1);
        input.setType("type2");
        input.setTripId("trip2");
        input.setUserName("user2");
        input.setTime(1L);
        input.setProcessed(false);

        saveId = dao.save(input);
        assertNotEquals(-1, saveId);
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void testSave() {
        RecordEntity input = new RecordEntity();
        input.setId(-1);
        input.setType("type");
        input.setTripId("trip");
        input.setUserName("user");
        input.setTime(1L);
        input.setProcessed(false);

        int inputId = dao.save(input);

        RecordEntity result = dao.get(inputId);
        assertNotNull(result);

        assertEquals(input.getType(), result.getType());
        assertEquals(input.getTripId(), result.getTripId());
        assertEquals(input.getUserName(), result.getUserName());
        assertEquals(input.getTime(), result.getTime());
        assertEquals(input.isProcessed(), result.isProcessed());
    }


    @Test
    public void testGetNumberOfRecords() {
        int result = dao.getNumberOfRecord(false);
        assertEquals(3, result);
    }

    @Test
    public void testGetByTripIdAndType(){
        List<RecordEntity> result = dao.getByTripIdAndType("trip", "type", false);

        assertNotNull(result);
        assertEquals(2, result.size());

        RecordEntity recordEntity = result.get(0);
        assertNotNull(recordEntity);

        assertEquals("type", recordEntity.getType());
        assertEquals("trip", recordEntity.getTripId());
        assertEquals("user", recordEntity.getUserName());
        assertEquals("1", recordEntity.getTime().toString());
        assertEquals(false, recordEntity.isProcessed());

    }

    @Test
    public void testUpdateProcessed(){
        List<RecordEntity> result = dao.getByTripIdAndType("trip", "type", false);

        assertNotNull(result);
        assertEquals(2, result.size());

        RecordEntity entity = result.get(0);
        assertNotNull(entity);
        int id = entity.getId();

        List<Integer> entities = new ArrayList<>();
        entities.add(id);

        dao.updateProcessed(entities, true);

        result = dao.getByTripIdAndType("trip", "type", true);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetDistinctTrips(){
        List<String> distinctTrips = dao.getDistinctTrips(false);

        assertNotNull(distinctTrips);
        assertEquals(2, distinctTrips.size());
    }

    @Test
    public void testGetRecordsDistinctTypesForTrip(){
        List<String> trip = dao.getRecordsDistinctTypesForTrip("trip");
        assertNotNull(trip);
        assertEquals(1, trip.size());
        assertEquals("type", trip.get(0));
    }
}
