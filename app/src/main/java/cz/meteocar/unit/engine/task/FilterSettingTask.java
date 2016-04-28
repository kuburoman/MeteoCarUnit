package cz.meteocar.unit.engine.task;

import android.util.Log;

import com.google.common.base.Converter;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.network.ErrorCodes;
import cz.meteocar.unit.engine.network.NetworkException;
import cz.meteocar.unit.engine.network.dto.CreateFilterSettingRequest;
import cz.meteocar.unit.engine.network.dto.FilterSettingDto;
import cz.meteocar.unit.engine.network.dto.GetFilterSettingResponse;
import cz.meteocar.unit.engine.network.task.NetworkConnector;
import cz.meteocar.unit.engine.network.task.QueryParameter;
import cz.meteocar.unit.engine.network.task.converter.FilterSettingsEntity2DtoConverter;
import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.helper.FilterSettingHelper;
import cz.meteocar.unit.engine.storage.model.FilterSettingEntity;

/**
 * Task for synchronization filter setting with server.
 */
public class FilterSettingTask extends AbstractTask {

    private NetworkConnector<Void, GetFilterSettingResponse> getConnector;
    private NetworkConnector<CreateFilterSettingRequest, Void> postConnector;

    private FilterSettingHelper dao;

    private static final Converter<FilterSettingEntity, FilterSettingDto> converterForward = new FilterSettingsEntity2DtoConverter();
    private static final Converter<FilterSettingDto, FilterSettingEntity> converterBackward = converterForward.reverse();

    public FilterSettingTask() {
        this(
                new NetworkConnector<>(Void.class, GetFilterSettingResponse.class, "filterSettings"),
                new NetworkConnector<>(CreateFilterSettingRequest.class, Void.class, "filterSettings"),
                ServiceManager.getInstance().getDB().getFilterSettingHelper());
    }

    public FilterSettingTask(NetworkConnector<Void, GetFilterSettingResponse> getConnector,
                             NetworkConnector<CreateFilterSettingRequest, Void> postConnector,
                             FilterSettingHelper dao) {
        this.getConnector = getConnector;
        this.postConnector = postConnector;
        this.dao = dao;
    }

    @Override
    public void runTask() {
        if (isNetworkReady()) {
            try {
                List<FilterSettingEntity> all = dao.getAll();
                Long updateTime = getLatestUpdateTime(all);

                List<QueryParameter> params = new ArrayList<>();
                params.add(new QueryParameter("lastUpdateTime", String.valueOf(updateTime)));
                GetFilterSettingResponse response = getConnector.get(params);
                if (response.getRecords().isEmpty()) {
                    return;
                }
                dao.deleteAll();
                dao.saveAll(Lists.newArrayList(converterBackward.convertAll(response.getRecords())));
            } catch (NetworkException e) {
                exceptionCaught(e);
            } catch (DatabaseException e) {
                Log.e(AppLog.LOG_TAG_DB, e.getMessage(), e);
            }
        }
    }

    protected void exceptionCaught(NetworkException e) {
        if (ErrorCodes.RECORDS_UPDATE_REQUIRED.toString().equals(e.getErrorResponse().getCode())) {
            try {
                postConnector.post(new CreateFilterSettingRequest(Lists.newArrayList(converterForward.convertAll(dao.getAll()))));
                return;
            } catch (NetworkException e1) {
                logException(e1);
            }
        }
        logException(e);
    }

    protected Long getLatestUpdateTime(List<FilterSettingEntity> filterSettingEntities) {
        Long max = 0L;
        for (FilterSettingEntity entity : filterSettingEntities) {
            if (max < entity.getUpdateTime()) {
                max = entity.getUpdateTime();
            }
        }
        return max;
    }
}

