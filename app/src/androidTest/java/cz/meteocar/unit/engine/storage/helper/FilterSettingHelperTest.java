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
import cz.meteocar.unit.engine.storage.model.FilterSettingEntity;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for {@link FilterSettingHelper}.
 */
@RunWith(AndroidJUnit4.class)
public class FilterSettingHelperTest {

    private DatabaseHelper db;
    private FilterSettingHelper dao;

    @Before
    public void setUp() throws Exception {

        RenamingDelegatingContext context = new RenamingDelegatingContext(getTargetContext(), "test_");
        context.deleteDatabase("test_" + MySQLiteConfig.DATABASE_NAME);

        db = new DatabaseHelper(context);
        dao = new FilterSettingHelper(db);
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void testSave() throws DatabaseException {
        FilterSettingEntity input = new FilterSettingEntity();
        input.setId(-1);
        input.setAlgorithm("ALGORITHM");
        input.setTag("TAG");
        input.setValue(1.1);
        input.setActive(false);
        input.setUpdateTime(1L);

        int inputId = dao.save(input);

        FilterSettingEntity result = dao.get(inputId);
        assertNotNull(result);

        assertEquals(input.getAlgorithm(), result.getAlgorithm());
        assertEquals(input.getTag(), result.getTag());
        assertEquals(input.getValue(), result.getValue());
        assertEquals(input.isActive(), result.isActive());
        assertEquals(input.getUpdateTime(), result.getUpdateTime());
    }
}
