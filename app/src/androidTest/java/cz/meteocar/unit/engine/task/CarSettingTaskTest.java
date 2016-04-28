package cz.meteocar.unit.engine.task;

import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import cz.meteocar.unit.engine.network.NetworkException;
import cz.meteocar.unit.engine.network.dto.CarSettingDto;
import cz.meteocar.unit.engine.network.dto.CreateCarSettingRequest;
import cz.meteocar.unit.engine.network.dto.ErrorResponse;
import cz.meteocar.unit.engine.network.dto.GetCarSettingResponse;
import cz.meteocar.unit.engine.network.task.NetworkConnector;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.helper.CarSettingHelper;
import cz.meteocar.unit.engine.storage.helper.DatabaseHelper;
import cz.meteocar.unit.engine.storage.model.CarSettingEntity;
import cz.meteocar.unit.engine.task.event.SyncWithServerChangedEvent;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
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
 * Test for {@link CarSettingTask}.
 */
@RunWith(AndroidJUnit4.class)
public class CarSettingTaskTest {

    private NetworkConnector<Void, GetCarSettingResponse> getConnector;
    private NetworkConnector<CreateCarSettingRequest, Void> postConnector;
    private CarSettingHelper dao;

    private DatabaseHelper db;

    private CarSettingTask task;

    @Before
    public void setUp() throws Exception {

        getConnector = mock(NetworkConnector.class);
        postConnector = mock(NetworkConnector.class);

        RenamingDelegatingContext context = new RenamingDelegatingContext(getTargetContext(), "test_");
        context.deleteDatabase("test_" + MySQLiteConfig.DATABASE_NAME);

        db = new DatabaseHelper(context);
        CarSettingHelper daoWithoutSpy = new CarSettingHelper(db);
        dao = spy(daoWithoutSpy);

        CarSettingTask taskWithoutSpy = new CarSettingTask(getConnector, postConnector, dao);
        task = spy(taskWithoutSpy);

        doReturn(true).when(task).isNetworkReady();
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
        CarSettingDto dto = new CarSettingDto();
        dto.setUpdateTime(1461603083946L);
        dto.setActive(true);
        dto.setCode("code");
        dto.setValue("value");

        GetCarSettingResponse response = new GetCarSettingResponse();
        response.getRecords().add(dto);
        when(getConnector.get(any(List.class))).thenReturn(response);
        doNothing().when(task).postEvent(any(SyncWithServerChangedEvent.class));

        task.runTask();

        List<CarSettingEntity> all = dao.getAll();
        assertNotNull(all);
        assertEquals(1, all.size());

        CarSettingEntity result = all.get(0);
        assertNotNull(result);

        assertEquals(1461603083946L, dto.getUpdateTime(), 0);
        assertEquals(true, dto.isActive());
        assertEquals("code", dto.getCode());
        assertEquals("value", dto.getValue());
        verifyNoMoreInteractions(postConnector);
    }

    @Test
    public void networkSaveVersion() throws NetworkException {
        when(getConnector.get(any(List.class))).thenReturn(new GetCarSettingResponse());

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
        verify(postConnector, times(1)).post(any(CreateCarSettingRequest.class));
    }


}