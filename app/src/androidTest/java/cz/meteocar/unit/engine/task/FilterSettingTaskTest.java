package cz.meteocar.unit.engine.task;

import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import cz.meteocar.unit.engine.network.NetworkException;
import cz.meteocar.unit.engine.network.dto.CreateFilterSettingRequest;
import cz.meteocar.unit.engine.network.dto.ErrorResponse;
import cz.meteocar.unit.engine.network.dto.FilterSettingDto;
import cz.meteocar.unit.engine.network.dto.GetFilterSettingResponse;
import cz.meteocar.unit.engine.network.task.NetworkConnector;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.helper.DatabaseHelper;
import cz.meteocar.unit.engine.storage.helper.FilterSettingHelper;
import cz.meteocar.unit.engine.storage.model.FilterSettingEntity;

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
 * Test for {@link FilterSettingTask}.
 */
@RunWith(AndroidJUnit4.class)
public class FilterSettingTaskTest {

    private FilterSettingTask task;
    private NetworkConnector<Void, GetFilterSettingResponse> getConnector;
    private NetworkConnector<CreateFilterSettingRequest, Void> postConnector;


    private FilterSettingHelper dao;
    private DatabaseHelper db;

    @Before
    public void setUp() throws Exception {

        getConnector = mock(NetworkConnector.class);
        postConnector = mock(NetworkConnector.class);

        RenamingDelegatingContext context = new RenamingDelegatingContext(getTargetContext(), "test_");
        context.deleteDatabase("test_" + MySQLiteConfig.DATABASE_NAME);

        db = new DatabaseHelper(context);
        FilterSettingHelper daoWithoutSpy = new FilterSettingHelper(db);
        dao = spy(daoWithoutSpy);

        FilterSettingTask taskWithoutSpy = new FilterSettingTask(getConnector, postConnector, dao);
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
        FilterSettingDto dto = new FilterSettingDto();
        dto.setUpdateTime(1461603083946L);
        dto.setValue(2.0);
        dto.setActive(true);
        dto.setAlgorithm("algorithm");
        dto.setTag("tag");

        GetFilterSettingResponse response = new GetFilterSettingResponse();
        response.getRecords().add(dto);
        when(getConnector.get(any(List.class))).thenReturn(response);

        task.runTask();

        List<FilterSettingEntity> all = dao.getAll();
        assertNotNull(all);
        assertEquals(1, all.size());

        FilterSettingEntity result = all.get(0);
        assertNotNull(result);

        assertEquals(1461603083946L, dto.getUpdateTime(), 0);
        assertEquals(2.0, dto.getValue(), 0);
        assertEquals(true, dto.isActive());
        assertEquals("algorithm", dto.getAlgorithm());
        assertEquals("tag", dto.getTag());

        verifyNoMoreInteractions(postConnector);
    }

    @Test
    public void networkSaveVersion() throws NetworkException {
        when(getConnector.get(any(List.class))).thenReturn(new GetFilterSettingResponse());

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
        verify(postConnector, times(1)).post(any(CreateFilterSettingRequest.class));
    }
}
