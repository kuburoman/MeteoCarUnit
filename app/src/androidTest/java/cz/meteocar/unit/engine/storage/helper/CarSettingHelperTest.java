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

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for {@link CarSettingHelper}.
 */
@RunWith(AndroidJUnit4.class)
public class CarSettingHelperTest {

    private DatabaseHelper db;
    private CarSettingHelper dao;

    @Before
    public void setUp() throws Exception {

        RenamingDelegatingContext context = new RenamingDelegatingContext(getTargetContext(), "test_");
        context.deleteDatabase("test_" + MySQLiteConfig.DATABASE_NAME);

        db = new DatabaseHelper(context);
        dao = new CarSettingHelper(db);
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void testSave() throws DatabaseException {
        CarSettingEntity input = new CarSettingEntity();
        input.setId(-1);
        input.setCode("CODE1");
        input.setValue("VALUE");
        input.setActive(false);
        input.setUpdateTime(1L);

        int inputId = dao.save(input);

        CarSettingEntity result = dao.get(inputId);
        assertNotNull(result);

        assertEquals(input.getCode(), result.getCode());
        assertEquals(input.getValue(), result.getValue());
        assertEquals(input.isActive(), result.isActive());
        assertEquals(input.getUpdateTime(), result.getUpdateTime());
    }

    @Test
    public void testGetByCode() throws DatabaseException {
        CarSettingEntity input = new CarSettingEntity();
        input.setId(-1);
        input.setCode("CODE1");
        input.setValue("VALUE");
        input.setActive(false);
        input.setUpdateTime(1L);

        dao.save(input);

        CarSettingEntity result = dao.getByCode("CODE1");

        assertNotNull(result);

        assertEquals(input.getCode(), result.getCode());
        assertEquals(input.getValue(), result.getValue());
        assertEquals(input.isActive(), result.isActive());
        assertEquals(input.getUpdateTime(), result.getUpdateTime());

    }

}
