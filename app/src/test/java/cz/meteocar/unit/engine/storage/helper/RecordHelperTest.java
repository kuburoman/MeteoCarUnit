package cz.meteocar.unit.engine.storage.helper;

import android.test.AndroidTestCase;

/**
 * Created by Nell on 30.3.2016.
 */
public class RecordHelperTest extends AndroidTestCase {

//    private DatabaseHelper db;
//    private RecordHelper dao;
//
//    @Override
//    public void setUp() throws Exception {
//        super.setUp();
//        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
//        db = new DatabaseHelper(context);
//        dao = new RecordHelper(db);
//    }
//
//    @Override
//    public void tearDown() throws Exception {
//        db.close();
//        super.tearDown();
//    }
//
//    public void testAddEntry() {
//        RecordEntity input = new RecordEntity();
//        input.setId(-1);
//        input.setType("type");
//        input.setTripId("trip");
//        input.setUserName("user");
//        input.setTime(1L);
//        input.setProcessed(false);
//
//        int inputId = dao.save(input);
//
//        RecordEntity result = dao.get(inputId);
//        assertNotNull(result);
//
//        assertEquals(input.getId(), result.getId());
//        assertEquals(input.getType(), result.getType());
//        assertEquals(input.getTripId(), result.getTripId());
//        assertEquals(input.getUserName(), result.getUserName());
//        assertEquals(input.getTime(), result.getTime());
//        assertEquals(input.isProcessed(), result.isProcessed());
//    }
}
