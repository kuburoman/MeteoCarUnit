package cz.meteocar.unit.engine.storage.helper;

import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.UserEntity;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for {@link CarSettingHelper}.
 */
@RunWith(AndroidJUnit4.class)
public class UserHelperTest {

    private DatabaseHelper db;
    private UserHelper dao;

    @Before
    public void setUp() throws Exception {

        RenamingDelegatingContext context = new RenamingDelegatingContext(getTargetContext(), "test_");
        context.deleteDatabase("test_" + MySQLiteConfig.DATABASE_NAME);

        db = new DatabaseHelper(context);
        dao = new UserHelper(db);
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void testSave() throws DatabaseException {
        UserEntity input = new UserEntity();
        input.setId(-1);
        input.setAdmin(false);
        input.setPassword("setPassword");
        input.setUsername("setUsername");

        int inputId = dao.save(input);

        UserEntity result = dao.get(inputId);
        assertNotNull(result);

        assertEquals(input.getPassword(), result.getPassword());
        assertEquals(input.getUsername(), result.getUsername());
        assertEquals(input.isAdmin(), result.isAdmin());
    }

    @Test
    public void testGetByUsername() throws DatabaseException {
        UserEntity input = new UserEntity();
        input.setId(-1);
        input.setAdmin(false);
        input.setPassword("setPassword");
        input.setUsername("setUsername");

        dao.save(input);

        UserEntity result = dao.getUser("setUsername");

        assertNotNull(result);
        assertEquals(input.getPassword(), result.getPassword());
        assertEquals(input.getUsername(), result.getUsername());
        assertEquals(input.isAdmin(), result.isAdmin());
    }

    @Test
    public void testGetByUsernameAndPassword() throws DatabaseException {
        UserEntity input = new UserEntity();
        input.setId(-1);
        input.setAdmin(false);
        input.setPassword("setPassword");
        input.setUsername("setUsername");

        dao.save(input);

        UserEntity result = dao.getUser("setUsername", "setPassword");

        assertNotNull(result);
        assertEquals(input.getPassword(), result.getPassword());
        assertEquals(input.getUsername(), result.getUsername());
        assertEquals(input.isAdmin(), result.isAdmin());
    }

}
