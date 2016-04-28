package cz.meteocar.unit.engine.task;

import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import cz.meteocar.unit.engine.network.NetworkException;
import cz.meteocar.unit.engine.network.dto.CreateOBDPidRequest;
import cz.meteocar.unit.engine.network.dto.ErrorResponse;
import cz.meteocar.unit.engine.network.dto.GetOBDPidResponse;
import cz.meteocar.unit.engine.network.dto.OBDPidDto;
import cz.meteocar.unit.engine.network.task.NetworkConnector;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.helper.DatabaseHelper;
import cz.meteocar.unit.engine.storage.helper.ObdPidHelper;
import cz.meteocar.unit.engine.storage.model.ObdPidEntity;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Test for {@link OBDPidsTask}.
 */
@RunWith(AndroidJUnit4.class)
public class OBDPidsTaskTest {

    private NetworkConnector<Void, GetOBDPidResponse> getConnector;
    private NetworkConnector<CreateOBDPidRequest, Void> postConnector;

    private ObdPidHelper dao;
    private DatabaseHelper db;

    private OBDPidsTask task;

    @Before
    public void setUp() throws Exception {

        getConnector = mock(NetworkConnector.class);
        postConnector = mock(NetworkConnector.class);

        RenamingDelegatingContext context = new RenamingDelegatingContext(getTargetContext(), "test_");
        context.deleteDatabase("test_" + MySQLiteConfig.DATABASE_NAME);

        db = new DatabaseHelper(context);
        ObdPidHelper daoWithoutSpy = new ObdPidHelper(db);
        dao = spy(daoWithoutSpy);

        OBDPidsTask taskWithoutSpy = new OBDPidsTask(getConnector, postConnector, dao);
        task = spy(taskWithoutSpy);

        doReturn(true).when(task).isNetworkReady();
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void networkOffline() {
        doReturn(false).when(task).isNetworkReady();
        task.runTask();

        verifyZeroInteractions(dao);
        verifyZeroInteractions(getConnector);
        verifyNoMoreInteractions(postConnector);
    }

    @Test
    public void networkLocalUpdate() throws NetworkException {
        OBDPidDto dto = new OBDPidDto();
        dto.setUpdateTime(1461603083946L);
        dto.setActive(true);
        dto.setTag("tag");
        dto.setPidCode("010D");
        dto.setMin(0);
        dto.setMax(255);
        dto.setName("name");
        dto.setFormula("A");

        GetOBDPidResponse response = new GetOBDPidResponse();
        response.getRecords().add(dto);
        when(getConnector.get(any(List.class))).thenReturn(response);

        task.runTask();

        List<ObdPidEntity> all = dao.getAll();
        assertNotNull(all);
        assertEquals(1, all.size());

        ObdPidEntity result = all.get(0);
        assertNotNull(result);

        assertEquals(1461603083946L, dto.getUpdateTime(), 0);
        assertEquals(true, dto.isActive());
        assertEquals("tag", dto.getTag());
        assertEquals("010D", dto.getPidCode());
        assertEquals(0, dto.getMin());
        assertEquals(255, dto.getMax());
        assertEquals("name", dto.getName());
        assertEquals("A", dto.getFormula());

        verifyNoMoreInteractions(postConnector);
    }

    @Test
    public void networkSaveVersion() throws NetworkException {
        when(getConnector.get(any(List.class))).thenReturn(new GetOBDPidResponse());

        task.runTask();

        verify(dao, times(1)).getAll();
        verify(dao, never()).deleteAll();
        verifyNoMoreInteractions(postConnector);
    }

    @Test
    public void networkServerUpdate() throws NetworkException {
        NetworkException exception = new NetworkException(new ErrorResponse("RECORDS_UPDATE_REQUIRED", "RECORDS_UPDATE_REQUIRED"));

        when(getConnector.get(any(List.class))).thenThrow(exception);

        task.runTask();

        verify(dao, times(2)).getAll();
        verify(dao, never()).deleteAll();
        verify(postConnector, times(1)).post(any(CreateOBDPidRequest.class));
    }

}