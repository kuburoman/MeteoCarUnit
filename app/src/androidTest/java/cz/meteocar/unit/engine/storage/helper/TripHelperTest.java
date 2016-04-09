package cz.meteocar.unit.engine.storage.helper;

import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.CarSettingEntity;
import cz.meteocar.unit.engine.storage.model.TripEntity;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for {@link CarSettingHelper}.
 */
@RunWith(AndroidJUnit4.class)
public class TripHelperTest {

    private DatabaseHelper db;
    private TripHelper dao;

    @Before
    public void setUp() throws Exception {

        RenamingDelegatingContext context = new RenamingDelegatingContext(getTargetContext(), "test_");
        context.deleteDatabase("test_" + MySQLiteConfig.DATABASE_NAME);

        db = new DatabaseHelper(context);
        dao = new TripHelper(db);
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void testSave() throws DatabaseException {
        TripEntity input = new TripEntity();
        input.setId(-1);
        input.setJson("JSON1");

        int inputId = dao.save(input);

        TripEntity result = dao.get(inputId);
        assertNotNull(result);

        assertEquals(input.getJson(), result.getJson());
    }

    @Test
    public void testGetOneTrip() throws DatabaseException {
        TripEntity input = new TripEntity();
        input.setId(-1);
        input.setJson("JSON1");
        dao.save(input);

        TripEntity input2 = new TripEntity();
        input2.setId(-1);
        input2.setJson("JSON2");
        dao.save(input2);

        TripEntity result = dao.getOneTrip();
        assertNotNull(result);
        assertEquals(2, dao.getNumberOfRecord());

        dao.delete(result.getId());

        assertEquals(1, dao.getNumberOfRecord());

        result = dao.getOneTrip();
        assertNotNull(result);
        assertEquals(1, dao.getNumberOfRecord());

        dao.delete(result.getId());

        assertEquals(0, dao.getNumberOfRecord());
    }

}
