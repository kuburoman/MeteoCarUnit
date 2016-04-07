package cz.meteocar.unit.engine.storage;

import android.util.Log;

import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.convertor.DataPoint2RecordEntityConverter;
import cz.meteocar.unit.engine.convertor.RecordEntity2DataPointConverter;
import cz.meteocar.unit.engine.enums.FilterEnum;
import cz.meteocar.unit.engine.event.DebugMessageEvent;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.helper.FilterSettingHelper;
import cz.meteocar.unit.engine.storage.helper.JsonTags;
import cz.meteocar.unit.engine.storage.helper.RecordHelper;
import cz.meteocar.unit.engine.storage.helper.TripHelper;
import cz.meteocar.unit.engine.storage.model.FilterSettingEntity;
import cz.meteocar.unit.engine.storage.model.RecordEntity;
import cz.meteocar.unit.engine.storage.model.TripEntity;
import cz.meteocar.unit.engine.storage.simplify.DataPoint;
import cz.meteocar.unit.engine.storage.simplify.PercentageSimplify;
import cz.meteocar.unit.engine.storage.simplify.RDPSimplify;
import cz.meteocar.unit.engine.task.AbstractTask;

/**
 * Created by Nell on 13.12.2015.
 */
public class RecordConvertTask extends AbstractTask {

    protected static final int NUMBER_OF_RECORDS_TO_SEND_TOGETHER = 100;

    private RecordHelper recordHelper;
    private TripHelper tripHelper;
    private FilterSettingHelper filterSettingHelper;
    private String debugMessage;

    private RecordEntity2DataPointConverter converterIntoPoint;
    private DataPoint2RecordEntityConverter converterFromPoint;

    private PercentageSimplify percentageSimplify;
    private RDPSimplify rdpSimplify;


    public RecordConvertTask() {
        this(ServiceManager.getInstance().getDB().getRecordHelper(),
                ServiceManager.getInstance().getDB().getTripHelper(),
                ServiceManager.getInstance().getDB().getFilterSettingHelper(),
                new RecordEntity2DataPointConverter(),
                new DataPoint2RecordEntityConverter(),
                new PercentageSimplify(),
                new RDPSimplify()
        );
    }

    public RecordConvertTask(RecordHelper recordHelper, TripHelper tripHelper, FilterSettingHelper filterSettingHelper,
                             RecordEntity2DataPointConverter converterIntoPoint, DataPoint2RecordEntityConverter converterFromPoint,
                             PercentageSimplify percentageSimplify, RDPSimplify rdpSimplify) {
        this.recordHelper = recordHelper;
        this.tripHelper = tripHelper;
        this.filterSettingHelper = filterSettingHelper;
        this.converterIntoPoint = converterIntoPoint;
        this.converterFromPoint = converterFromPoint;
        this.percentageSimplify = percentageSimplify;
        this.rdpSimplify = rdpSimplify;
    }

    @Override
    public void runTask() {
        if (recordHelper.getNumberOfRecord(false) > 0) {
            createJsonRecords();
        }
    }

    protected JSONObject createJsonTrip(List<RecordEntity> recordList) throws JSONException {

        JSONArray jsonArray = new JSONArray();

        String userName = recordList.get(0).getUserName();
        String tripId = recordList.get(0).getTripId();

        for (RecordEntity recordEntity : recordList) {
            JSONObject object = new JSONObject();

            JSONObject value = new JSONObject(recordEntity.getJson());
            object.put(JsonTags.JSON, value);
            object.put(JsonTags.CODE, recordEntity.getType());
            object.put(JsonTags.TIME, recordEntity.getTime());

            jsonArray.put(object);
        }


        JSONObject main = new JSONObject();
        main.put(JsonTags.TRIP, tripId);
        main.put(JsonTags.USER, userName);
        main.put(JsonTags.RECORDS, jsonArray);

        return main;
    }

    protected void createJsonRecords() {
        List<String> trips = recordHelper.getDistinctTrips(false);
        for (String trip : trips) {
            List<String> types = recordHelper.getRecordsDistinctTypesForTrip(trip);

            debugMessage = "trip: " + trip + "\n";

            for (String type : types) {
                List<RecordEntity> records = recordHelper.getByTripIdAndType(trip, type, false);
                List<RecordEntity> simplifiedRecords = simplifyRecords(type, records);
                debugMessage += type + ": " + simplifiedRecords.size() + "/" + records.size() + "\n";
                deleteRemovedRecords(records, simplifiedRecords);
                try {
                    convertRecordsIntoTrip(simplifiedRecords, NUMBER_OF_RECORDS_TO_SEND_TOGETHER);
                } catch (DatabaseException | JSONException e) {
                    Log.e(AppLog.LOG_TAG_DB, e.getMessage(), e);
                    return;
                }
            }
            postDebugMessage(debugMessage);
        }
    }

    protected void postDebugMessage(String debugMessage) {
        if (DB.getShowFilterResults()) {
            ServiceManager.getInstance().eventBus.post(
                    new DebugMessageEvent(debugMessage)
            ).asynchronously();
        }
    }

    protected void convertRecordsIntoTrip(List<RecordEntity> inputList, int partitionSize) throws DatabaseException, JSONException {
        List<List<RecordEntity>> partitions = Lists.partition(inputList, partitionSize);
        for (List<RecordEntity> partition : partitions) {
            processPartition(partition);
        }
    }

    protected void processPartition(List<RecordEntity> partition) throws JSONException, DatabaseException {

        JSONObject jsonTrip = createJsonTrip(partition);
        tripHelper.save(new TripEntity(-1, jsonTrip.toString()));

        List<Integer> integers = new ArrayList<>();
        for (RecordEntity recordEntity : partition) {
            integers.add(recordEntity.getId());
        }

        recordHelper.updateProcessed(integers, true);
    }

    protected void deleteRemovedRecords(List<RecordEntity> fullList, List<RecordEntity> simplifiedList) {
        fullList.removeAll(simplifiedList);
        recordHelper.deleteAll(fullList);
    }

    protected List<RecordEntity> simplifyRecords(String type, List<RecordEntity> input) {
        FilterSettingEntity filter = filterSettingHelper.getByCode(type);
        if (filter == null) {
            return new ArrayList<>(input);
        }
        if (!filter.isActive()) {
            return new ArrayList<>(input);
        }

        List<DataPoint> dataPoints = converterIntoPoint.convertList(input);

        if (FilterEnum.PERCENTAGE.toString().equals(filter.getAlgorithm())) {
            List<DataPoint> simplified = percentageSimplify.simplify(dataPoints, filter.getValue());
            return converterFromPoint.convertList(simplified);
        }

        if (FilterEnum.RDP.toString().equals(filter.getAlgorithm())) {
            return converterFromPoint.convertList(rdpSimplify.simplify(dataPoints, filter.getValue()));
        }
        return new ArrayList<>(input);
    }
}
