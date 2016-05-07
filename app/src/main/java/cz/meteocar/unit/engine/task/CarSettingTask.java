package cz.meteocar.unit.engine.task;

import android.util.Log;

import com.google.common.base.Converter;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.network.ErrorCodes;
import cz.meteocar.unit.engine.network.NetworkException;
import cz.meteocar.unit.engine.network.dto.CarSettingDto;
import cz.meteocar.unit.engine.network.dto.CreateCarSettingRequest;
import cz.meteocar.unit.engine.network.dto.GetCarSettingResponse;
import cz.meteocar.unit.engine.network.task.NetworkConnector;
import cz.meteocar.unit.engine.network.task.QueryParameter;
import cz.meteocar.unit.engine.network.task.converter.CarSettingsEntity2DtoConverter;
import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.helper.CarSettingHelper;
import cz.meteocar.unit.engine.storage.model.CarSettingEntity;
import cz.meteocar.unit.engine.task.event.SyncWithServerChangedEvent;

/**
 * Task for synchronization of Car Settings.
 */
public class CarSettingTask extends AbstractTask {

    private NetworkConnector<Void, GetCarSettingResponse> getConnector;
    private NetworkConnector<CreateCarSettingRequest, Void> postConnector;

    private CarSettingHelper dao;

    private static final Converter<CarSettingEntity, CarSettingDto> converterForward = new CarSettingsEntity2DtoConverter();
    private static final Converter<CarSettingDto, CarSettingEntity> converterBackward = converterForward.reverse();

    public CarSettingTask() {
        this(
                new NetworkConnector<>(Void.class, GetCarSettingResponse.class, "carSettings"),
                new NetworkConnector<>(CreateCarSettingRequest.class, Void.class, "carSettings"),
                ServiceManager.getInstance().getDB().getCarSettingHelper()
        );
    }

    public CarSettingTask(NetworkConnector<Void, GetCarSettingResponse> getConnector, NetworkConnector<CreateCarSettingRequest, Void> postConnector, CarSettingHelper dao) {
        this.getConnector = getConnector;
        this.postConnector = postConnector;
        this.dao = dao;
    }

    @Override
    public void runTask() {
        if (isNetworkReady()) {
            try {
                List<CarSettingEntity> all = dao.getAll();
                Long updateTime = getLatestUpdateTime(all);

                List<QueryParameter> params = new ArrayList<>();
                params.add(new QueryParameter("lastUpdateTime", String.valueOf(updateTime)));
                GetCarSettingResponse response = getConnector.get(params);
                if (response.getRecords().isEmpty()) {
                    return;
                }
                dao.deleteAll();
                dao.saveAll(Lists.newArrayList(converterBackward.convertAll(response.getRecords())));

                postEvent(new SyncWithServerChangedEvent());

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
                postConnector.post(new CreateCarSettingRequest(Lists.newArrayList(converterForward.convertAll(dao.getAll()))));
                return;
            } catch (NetworkException exception) {
                logException(exception);
                return;
            }
        }
        logException(e);
    }

    protected void postEvent(AppEvent event) {
        ServiceManager.getInstance().eventBus.post(event).asynchronously();
    }

    protected Long getLatestUpdateTime(List<CarSettingEntity> carSettingEntities) {
        Long max = 0L;
        for (CarSettingEntity entity : carSettingEntities) {
            if (max < entity.getUpdateTime()) {
                max = entity.getUpdateTime();
            }
        }
        return max;
    }
}


