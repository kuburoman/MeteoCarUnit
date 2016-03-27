package cz.meteocar.unit.engine.task;

import net.engio.mbassy.listener.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.enums.CarSettingEnum;
import cz.meteocar.unit.engine.network.task.CarSettingTask;
import cz.meteocar.unit.engine.network.task.DTCTask;
import cz.meteocar.unit.engine.network.task.FilterSettingTask;
import cz.meteocar.unit.engine.network.task.OBDPidsTask;
import cz.meteocar.unit.engine.network.task.PostTripTask;
import cz.meteocar.unit.engine.obd.taks.DTCRequestTask;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.RecordConvertTask;
import cz.meteocar.unit.engine.storage.helper.CarSettingHelper;
import cz.meteocar.unit.engine.storage.model.CarSettingEntity;
import cz.meteocar.unit.engine.task.event.RescheduleTasksEvent;
import cz.meteocar.unit.engine.task.event.SyncWithServerChangedEvent;

/**
 * Holds reference to all used Timer tasks and can manage them.
 */
public class TaskManager {

    private static final long DEFAULT_TIME = 10;
    private static final boolean INTERRUPT_RUNNING = false;

    private ScheduledFuture scheduledFilterSettingTask;
    private ScheduledFuture scheduledConvertService;
    private ScheduledFuture scheduledPostTripTask;
    private ScheduledFuture scheduledObdPidsTask;
    private ScheduledFuture scheduledCarSettingTask;
    private ScheduledFuture scheduledDtcRequestTask;
    private ScheduledFuture scheduledDtcTask;

    private ScheduledExecutorService service;


    private CarSettingHelper dao;

    List<AbstractTask> tasks;

    public TaskManager() {
        dao = ServiceManager.getInstance().db.getCarSettingHelper();
        service = Executors.newScheduledThreadPool(10);

        tasks = new ArrayList<>();

        ServiceManager.getInstance().eventBus.subscribe(this);

        scheduleAllTasks();
    }

    protected void scheduleAllTasks() {
        if (getSyncSwitch()) {
            startSyncTasks();
        }
        startOtherTasks();
    }

    protected boolean getSyncSwitch() {
        return DB.getSyncWithServer();
    }

    protected void startSyncTasks() {

        scheduledFilterSettingTask = service.scheduleAtFixedRate(new FilterSettingTask(), 0,
                getTimeForTask(CarSettingEnum.FILTER_SETTING_SYNC_PERIOD.toString()), TimeUnit.SECONDS);

        scheduledObdPidsTask = service.scheduleAtFixedRate(new OBDPidsTask(), 0,
                getTimeForTask(CarSettingEnum.OBD_PID_SYNC_PERIOD.toString()), TimeUnit.SECONDS);

        scheduledCarSettingTask = service.scheduleAtFixedRate(new CarSettingTask(), 0,
                getTimeForTask(CarSettingEnum.CAR_SETTING_SYNC_PERIOD.toString()), TimeUnit.SECONDS);

        scheduledDtcTask = service.scheduleAtFixedRate(new DTCTask(), 0,
                getTimeForTask(CarSettingEnum.DTC_SYNC_PERIOD.toString()), TimeUnit.SECONDS);
    }

    protected void startOtherTasks() {
        scheduledPostTripTask = service.scheduleAtFixedRate(new PostTripTask(), 0,
                getTimeForTask(CarSettingEnum.POST_TRIP_PERIOD.toString()), TimeUnit.SECONDS);

        scheduledDtcRequestTask = service.scheduleAtFixedRate(new DTCRequestTask(), 0,
                getTimeForTask(CarSettingEnum.DTC_REQUEST_PERIOD.toString()), TimeUnit.SECONDS);

        scheduledConvertService = service.scheduleAtFixedRate(new RecordConvertTask(), 0,
                getTimeForTask(CarSettingEnum.RECORD_CONVERT_PERIOD.toString()), TimeUnit.SECONDS);
    }

    protected long getTimeForTask(String code) {
        CarSettingEntity byCode = dao.getByCode(code);
        if (byCode != null && Long.getLong(byCode.getValue()) != null) {
            return Long.getLong(byCode.getValue());
        } else {
            return DEFAULT_TIME;
        }
    }

    protected void stopSyncTasks() {
        cancelTask(scheduledFilterSettingTask);
        cancelTask(scheduledConvertService);
        cancelTask(scheduledObdPidsTask);
        cancelTask(scheduledCarSettingTask);
        cancelTask(scheduledDtcRequestTask);
        cancelTask(scheduledDtcTask);
    }

    protected void stopOtherTasks() {
        cancelTask(scheduledPostTripTask);
        cancelTask(scheduledDtcRequestTask);
        cancelTask(scheduledConvertService);
    }

    protected void cancelTask(ScheduledFuture task) {
        if (task != null) {
            task.cancel(INTERRUPT_RUNNING);
        }
    }

    @Handler
    public void handleSyncChanged(SyncWithServerChangedEvent event) {
        stopSyncTasks();
        if (getSyncSwitch()) {
            startSyncTasks();
        }
    }

    @Handler
    public void handleRescheduleTasks(RescheduleTasksEvent event) {
        stopSyncTasks();
        stopOtherTasks();

        scheduleAllTasks();
    }
}
