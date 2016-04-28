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
import cz.meteocar.unit.engine.network.dto.CreateOBDPidRequest;
import cz.meteocar.unit.engine.network.dto.GetOBDPidResponse;
import cz.meteocar.unit.engine.network.dto.OBDPidDto;
import cz.meteocar.unit.engine.network.task.NetworkConnector;
import cz.meteocar.unit.engine.network.task.QueryParameter;
import cz.meteocar.unit.engine.network.task.converter.OBDPidEntity2DtoConverter;
import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.helper.ObdPidHelper;
import cz.meteocar.unit.engine.storage.model.ObdPidEntity;
import cz.meteocar.unit.engine.task.AbstractTask;

/**
 * Task for synchronization of OBD Pids.
 */
public class OBDPidsTask extends AbstractTask {

    private NetworkConnector<Void, GetOBDPidResponse> getConnector;
    private NetworkConnector<CreateOBDPidRequest, Void> postConnector;

    private ObdPidHelper dao;

    private static final Converter<ObdPidEntity, OBDPidDto> converterForward = new OBDPidEntity2DtoConverter();
    private static final Converter<OBDPidDto, ObdPidEntity> converterBackward = converterForward.reverse();

    public OBDPidsTask() {
        this(
                new NetworkConnector<>(Void.class, GetOBDPidResponse.class, "obdPids"),
                new NetworkConnector<>(CreateOBDPidRequest.class, Void.class, "obdPids"),
                ServiceManager.getInstance().getDB().getObdPidHelper()
        );
    }

    public OBDPidsTask(NetworkConnector<Void, GetOBDPidResponse> getConnector, NetworkConnector<CreateOBDPidRequest, Void> postConnector, ObdPidHelper dao) {
        this.getConnector = getConnector;
        this.postConnector = postConnector;
        this.dao = dao;
    }

    @Override
    public void runTask() {
        if (isNetworkReady()) {
            try {
                List<ObdPidEntity> all = dao.getAll();
                Long updateTime = getLatestUpdateTime(all);

                List<QueryParameter> params = new ArrayList<>();
                params.add(new QueryParameter("lastUpdateTime", String.valueOf(updateTime)));
                GetOBDPidResponse response = getConnector.get(params);
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
                postConnector.post(new CreateOBDPidRequest(Lists.newArrayList(converterForward.convertAll(dao.getAll()))));
                return;
            } catch (NetworkException e1) {
                Log.e(AppLog.LOG_TAG_NETWORK, e.getMessage(), e1);
                postNetworkException(e);
            }
        }
        Log.e(AppLog.LOG_TAG_NETWORK, e.getMessage(), e);
        postNetworkException(e);
    }

    protected Long getLatestUpdateTime(List<ObdPidEntity> OBDPidEntities) {
        Long max = 0L;
        for (ObdPidEntity entity : OBDPidEntities) {
            if (max < entity.getUpdateTime()) {
                max = entity.getUpdateTime();
            }
        }
        return max;
    }
}

