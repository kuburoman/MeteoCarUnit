package cz.meteocar.unit.engine.storage.helper;

import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.ObdPidEntity;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for {@link ObdPidHelper}.
 */
@RunWith(AndroidJUnit4.class)
public class ObdPidHelperTest {

    public DatabaseHelper db;
    public ObdPidHelper dao;


    @Before
    public void setUp() throws Exception {

        RenamingDelegatingContext context = new RenamingDelegatingContext(getTargetContext(), "test_");
        context.deleteDatabase("test_" + MySQLiteConfig.DATABASE_NAME);

        db = new DatabaseHelper(context);
        dao = new ObdPidHelper(db);
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void testSave() throws DatabaseException {
        ObdPidEntity input = new ObdPidEntity();
        input.setId(-1);
        input.setName("setName");
        input.setTag("setTag");
        input.setPidCode("setPidCode");
        input.setFormula("setFormula");
        input.setMin(1);
        input.setMax(2);
        input.setActive(false);
        input.setUpdateTime(1L);

        int inputId = dao.save(input);

        ObdPidEntity result = dao.get(inputId);
        assertNotNull(result);

        assertEquals(input.getName(), result.getName());
        assertEquals(input.getTag(), result.getTag());
        assertEquals(input.getPidCode(), result.getPidCode());
        assertEquals(input.getFormula(), result.getFormula());
        assertEquals(input.getMin(), result.getMin());
        assertEquals(input.getMax(), result.getMax());
        assertEquals(input.isActive(), result.isActive());
        assertEquals(input.getUpdateTime(), result.getUpdateTime());
    }


    @Test
    public void testGetAllActive() throws DatabaseException {
        dao.deleteAll();

        ObdPidEntity input = new ObdPidEntity();
        input.setId(-1);
        input.setName("setName");
        input.setTag("setTag");
        input.setPidCode("setPidCode");
        input.setFormula("setFormula");
        input.setMin(1);
        input.setMax(2);
        input.setActive(true);
        input.setUpdateTime(1L);
        dao.save(input);

        ObdPidEntity input2 = new ObdPidEntity();
        input2.setId(-1);
        input2.setName("setName2");
        input2.setTag("setTag2");
        input2.setPidCode("setPidCode2");
        input2.setFormula("setFormula2");
        input2.setMin(12);
        input2.setMax(22);
        input2.setActive(true);
        input2.setUpdateTime(12L);
        dao.save(input2);

        List<ObdPidEntity> result = dao.getAllActive();
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
