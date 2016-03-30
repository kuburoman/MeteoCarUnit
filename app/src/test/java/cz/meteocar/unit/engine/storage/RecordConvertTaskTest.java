package cz.meteocar.unit.engine.storage;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.convertor.DataPoint2RecordEntityConverter;
import cz.meteocar.unit.engine.convertor.RecordEntity2DataPointConverter;
import cz.meteocar.unit.engine.enums.FilterEnum;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class RecordConvertTaskTest {

    @Mock
    private RecordHelper recordHelper;

    @Mock
    private TripHelper tripHelper;

    @Mock
    private FilterSettingHelper filterSettingHelper;

    @Mock
    private RecordEntity2DataPointConverter converterIntoPoint;

    @Mock
    private DataPoint2RecordEntityConverter converterFromPoint;

    @Mock
    private PercentageSimplify percentageSimplify;

    @Mock
    private RDPSimplify rdpSimplify;

    @InjectMocks
    @Spy
    private RecordConvertTask convertService;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testRunTaskSuccessful() {
        when(recordHelper.getNumberOfRecord(false)).thenReturn(1);
        doNothing().when(convertService).createJsonRecords();

        convertService.runTask();

        verify(recordHelper).getNumberOfRecord(false);
        verify(convertService).runTask();
        verify(convertService).createJsonRecords();
        verifyNoMoreInteractions(recordHelper);
        verifyNoMoreInteractions(convertService);
    }

    @Test
    public void testCreateJsonRecordsNullUserId() {
        List<String> list = new ArrayList<>();
        list.add(null);
        when(recordHelper.getUserIdStored()).thenReturn(list);

        convertService.createJsonRecords();

        verify(convertService).createJsonRecords();
        verify(recordHelper).getUserIdStored();
        verify(recordHelper).deleteUserNullRecords();
        verifyNoMoreInteractions(convertService);
        verifyNoMoreInteractions(recordHelper);

    }

    @Test
    public void testCreateJsonRecordsSuccessful() throws DatabaseException, JSONException {
        List<String> users = new ArrayList<>();
        users.add("user");
        when(recordHelper.getUserIdStored()).thenReturn(users);

        List<String> types = new ArrayList<>();
        types.add("types");
        when(recordHelper.getRecordsDistinctTypesForUser("user")).thenReturn(types);


        List<RecordEntity> records = new ArrayList<>();
        records.add(new RecordEntity());
        when(recordHelper.getByUserIdAndType("user", "types", false)).thenReturn(records);

        doReturn(records).when(convertService).simplifyRecords("types", records);
        doNothing().when(convertService).convertRecordsIntoTrip(records, RecordConvertTask.NUMBER_OF_RECORDS_TO_SEND_TOGETHER);
        doNothing().when(convertService).deleteRemovedRecords(records, records);


        convertService.createJsonRecords();


        verify(recordHelper).getUserIdStored();
        verify(recordHelper).getRecordsDistinctTypesForUser("user");
        verify(recordHelper).getByUserIdAndType("user", "types", false);
        verifyNoMoreInteractions(recordHelper);
        verify(convertService).createJsonRecords();
        verify(convertService).simplifyRecords("types", records);
        verify(convertService).deleteRemovedRecords(records, records);
        verify(convertService).convertRecordsIntoTrip(records, RecordConvertTask.NUMBER_OF_RECORDS_TO_SEND_TOGETHER);
        verifyNoMoreInteractions(convertService);
    }

    @Test
    public void testCreateJsonRecordsException() throws DatabaseException, JSONException {
        List<String> users = new ArrayList<>();
        users.add("user");
        users.add("user2");
        when(recordHelper.getUserIdStored()).thenReturn(users);

        List<String> types = new ArrayList<>();
        types.add("types");
        when(recordHelper.getRecordsDistinctTypesForUser("user")).thenReturn(types);


        List<RecordEntity> records = new ArrayList<>();
        records.add(new RecordEntity());
        when(recordHelper.getByUserIdAndType("user", "types", false)).thenReturn(records);
        doNothing().when(convertService).deleteRemovedRecords(records, records);

        doReturn(records).when(convertService).simplifyRecords("types", records);
        doThrow(new DatabaseException()).when(convertService).convertRecordsIntoTrip(records, RecordConvertTask.NUMBER_OF_RECORDS_TO_SEND_TOGETHER);


        convertService.createJsonRecords();


        verify(recordHelper, times(1)).getUserIdStored();
        verify(recordHelper, times(1)).getRecordsDistinctTypesForUser("user");
        verify(recordHelper, times(1)).getByUserIdAndType("user", "types", false);
        verifyNoMoreInteractions(recordHelper);
        verify(convertService, times(1)).createJsonRecords();
        verify(convertService, times(1)).simplifyRecords("types", records);
        verify(convertService, times(1)).convertRecordsIntoTrip(records, RecordConvertTask.NUMBER_OF_RECORDS_TO_SEND_TOGETHER);
        verify(convertService, times(1)).deleteRemovedRecords(records, records);
        verifyNoMoreInteractions(convertService);
    }

    @Test
    public void testRunTaskNoInteraction() {
        when(recordHelper.getNumberOfRecord(false)).thenReturn(0);

        convertService.runTask();

        verify(recordHelper).getNumberOfRecord(false);
        verify(convertService).runTask();
        verifyNoMoreInteractions(recordHelper);
        verifyNoMoreInteractions(convertService);
    }

    @Test
    public void testCreateJsonTrip() throws JSONException {
        List<RecordEntity> entities = new ArrayList<>();

        RecordEntity firstEntity = new RecordEntity();
        firstEntity.setUserName("user");
        firstEntity.setTripId("trip");
        firstEntity.setTime(1L);
        firstEntity.setProcessed(false);
        JSONObject first = new JSONObject();
        first.put("pokus", false);
        firstEntity.setJson(first.toString());
        firstEntity.setType("type");

        entities.add(firstEntity);


        RecordEntity secondEntity = new RecordEntity();
        secondEntity.setUserName("user");
        secondEntity.setTripId("trip");
        secondEntity.setTime(2L);
        secondEntity.setProcessed(false);
        JSONObject second = new JSONObject();
        second.put("pokus", true);
        secondEntity.setJson(second.toString());
        secondEntity.setType("type2");

        entities.add(secondEntity);

        JSONObject jsonTrip = convertService.createJsonTrip(entities);

        assertNotNull(jsonTrip);
        assertTrue(jsonTrip.has(JsonTags.TRIP));
        assertEquals(firstEntity.getTripId(), jsonTrip.getString(JsonTags.TRIP));
        assertTrue(jsonTrip.has(JsonTags.USER));
        assertEquals(firstEntity.getUserName(), jsonTrip.getString(JsonTags.USER));
        assertTrue(jsonTrip.has(JsonTags.RECORDS));

        JSONArray jsonArray = jsonTrip.getJSONArray(JsonTags.RECORDS);
        assertNotNull(jsonArray);
        assertEquals(2, jsonArray.length());

        JSONObject jsonObject = jsonArray.getJSONObject(0);
        assertNotNull(jsonObject);
        assertTrue(jsonObject.has(JsonTags.TIME));
        assertEquals(firstEntity.getTime(), (Long) jsonObject.getLong(JsonTags.TIME));
        assertTrue(jsonObject.has(JsonTags.CODE));
        assertEquals(firstEntity.getType(), jsonObject.getString(JsonTags.CODE));
        assertTrue(jsonObject.has(JsonTags.JSON));

        jsonObject = jsonArray.getJSONObject(1);
        assertNotNull(jsonObject);
        assertTrue(jsonObject.has(JsonTags.TIME));
        assertEquals(secondEntity.getTime(), (Long) jsonObject.getLong(JsonTags.TIME));
        assertTrue(jsonObject.has(JsonTags.CODE));
        assertEquals(secondEntity.getType(), jsonObject.getString(JsonTags.CODE));
        assertTrue(jsonObject.has(JsonTags.JSON));
    }

    @Test
    public void testConvertRecordsIntoTrip() throws DatabaseException, JSONException {
        List<RecordEntity> input = new ArrayList<>();
        input.add(new RecordEntity());
        input.add(new RecordEntity());
        input.add(new RecordEntity());
        input.add(new RecordEntity());
        input.add(new RecordEntity());

        doNothing().when(convertService).processPartition(anyList());

        convertService.convertRecordsIntoTrip(input, 3);

        verify(convertService).convertRecordsIntoTrip(input, 3);
        verify(convertService, times(2)).processPartition(anyList());
        verifyNoMoreInteractions(convertService);
    }

    @Test
    public void testProcessPartition() throws JSONException, DatabaseException {

        List<RecordEntity> recordEntities = new ArrayList<>();
        recordEntities.add(new RecordEntity());

        doReturn(new JSONObject()).when(convertService).createJsonTrip(recordEntities);

        convertService.processPartition(recordEntities);

        verify(convertService).processPartition(recordEntities);
        verify(convertService).createJsonTrip(recordEntities);
        verifyNoMoreInteractions(convertService);
        verify(tripHelper).save(any(TripEntity.class));
        verifyNoMoreInteractions(tripHelper);
        verify(recordHelper).updateProcessed(anyList(), eq(true));
        verifyNoMoreInteractions(recordHelper);
    }

    @Test
    public void testDeleteRemovedRecords() {
        RecordEntity record1 = new RecordEntity();
        record1.setId(1);

        RecordEntity record2 = new RecordEntity();
        record2.setId(2);

        RecordEntity record3 = new RecordEntity();
        record3.setId(3);

        List<RecordEntity> full = new ArrayList<>();
        full.add(record1);
        full.add(record2);
        full.add(record3);

        List<RecordEntity> simplified = new ArrayList<>();
        simplified.add(record1);
        simplified.add(record2);

        doNothing().when(recordHelper).deleteAll(full);

        convertService.deleteRemovedRecords(full, simplified);

        assertEquals(1, full.size());
        verify(recordHelper).deleteAll(full);
        verifyNoMoreInteractions(recordHelper);
    }

    @Test
    public void testSimplifyRecordsFilterNotFound() {
        List<RecordEntity> input = new ArrayList<>();

        when(filterSettingHelper.getByCode("type")).thenReturn(null);

        assertEquals(input, convertService.simplifyRecords("type", input));

        verifyZeroInteractions(percentageSimplify);
        verifyZeroInteractions(rdpSimplify);
        verifyZeroInteractions(converterIntoPoint);
        verifyZeroInteractions(converterFromPoint);
    }

    @Test
    public void testSimplifyRecordsFilterNotActive() {
        List<RecordEntity> input = new ArrayList<>();

        FilterSettingEntity filterSettingEntity = new FilterSettingEntity();
        filterSettingEntity.setActive(false);

        when(filterSettingHelper.getByCode("type")).thenReturn(filterSettingEntity);

        assertEquals(input, convertService.simplifyRecords("type", input));

        verifyZeroInteractions(percentageSimplify);
        verifyZeroInteractions(rdpSimplify);
        verifyZeroInteractions(converterIntoPoint);
        verifyZeroInteractions(converterFromPoint);
    }

    @Test
    public void testSimplifyRecordsFilterRDP() {
        List<RecordEntity> input = new ArrayList<>();

        List<DataPoint> dataPoints = new ArrayList<>();

        FilterSettingEntity filterSettingEntity = new FilterSettingEntity();
        filterSettingEntity.setActive(false);
        filterSettingEntity.setAlgorithm(FilterEnum.RDP.toString());
        filterSettingEntity.setValue(1.1);

        when(filterSettingHelper.getByCode("type")).thenReturn(filterSettingEntity);
        when(converterIntoPoint.convertList(input)).thenReturn(dataPoints);
        when(rdpSimplify.simplify(dataPoints, 1.1)).thenReturn(dataPoints);
        when(converterFromPoint.convertList(dataPoints)).thenReturn(input);

        assertEquals(input, convertService.simplifyRecords("type", input));

        verifyZeroInteractions(percentageSimplify);
    }

    @Test
    public void testSimplifyRecordsFilterPercentage() {
        List<RecordEntity> input = new ArrayList<>();

        List<DataPoint> dataPoints = new ArrayList<>();

        FilterSettingEntity filterSettingEntity = new FilterSettingEntity();
        filterSettingEntity.setActive(false);
        filterSettingEntity.setAlgorithm(FilterEnum.RDP.toString());
        filterSettingEntity.setValue(1.1);

        when(filterSettingHelper.getByCode("type")).thenReturn(filterSettingEntity);
        when(converterIntoPoint.convertList(input)).thenReturn(dataPoints);
        when(percentageSimplify.simplify(dataPoints, 1.1)).thenReturn(dataPoints);
        when(converterFromPoint.convertList(dataPoints)).thenReturn(input);

        assertEquals(input, convertService.simplifyRecords("type", input));

        verifyZeroInteractions(rdpSimplify);
    }
}