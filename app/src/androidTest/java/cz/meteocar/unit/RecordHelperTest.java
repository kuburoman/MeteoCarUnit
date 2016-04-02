package cz.meteocar.unit.engine.storage.helper;

import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.RecordEntity;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Nell on 30.3.2016.
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
}
