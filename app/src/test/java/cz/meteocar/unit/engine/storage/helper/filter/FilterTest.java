package cz.meteocar.unit.engine.storage.helper.filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import cz.meteocar.unit.engine.convertor.RecordVO2EntityConverter;
import cz.meteocar.unit.engine.storage.helper.FilterSettingHelper;
import cz.meteocar.unit.engine.storage.helper.RecordHelper;
import cz.meteocar.unit.engine.storage.model.FilterSettingEntity;
import cz.meteocar.unit.engine.storage.model.RecordEntity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FilterTest {

//    @Mock
//    private FilterSettingHelper filterSettingHelper;
//
//    @Mock
//    private RecordHelper recordHelper;
//
//    @Mock
//    private RecordVO2EntityConverter converter;
//
//    private Filter filter;
//
//    private FilterSettingEntity filterSetting;
//
//    private RecordVO mainRecord = new RecordVO();
//
//    private RecordEntity entityRecord = new RecordEntity();
//
//    @Before
//    public void setUp() {
//
//        filter = new Filter(filterSettingHelper, recordHelper);
//        filter.setConverter(converter);
//
//        filterSetting = new FilterSettingEntity();
//        filterSetting.setReduceValue(4.0);
//        filterSetting.setRoundingDecimal(3);
//        filterSetting.setReduceType(ReducerType.PERCENTAGE);
//        filterSetting.setMaxTime(2000L);
//        filterSetting.setObdCode("type");
//        filterSetting.setRounding(true);
//        filterSetting.setActive(true);
//        filterSetting.setUpdateTime(2000L);
//
//        when(filterSettingHelper.getByCode(filterSetting.getObdCode())).thenReturn(filterSetting);
//
//        filter.records = new HashMap<>();
//        mainRecord.setUserId("user");
//        mainRecord.setTripId("trip");
//        mainRecord.setType("type");
//        mainRecord.setTime(1000L);
//        mainRecord.setLastSaved(1000L);
//        mainRecord.setValue(1.5);
//        mainRecord.setSaved(false);
//    }
//
//    @Test
//    public void testProcessFirst() {
//        when(converter.convert(mainRecord)).thenReturn(entityRecord);
//
//        filter.process(mainRecord);
//
//        verify(recordHelper).save(entityRecord);
//
//        assertTrue(filter.records.containsKey(mainRecord.getType()));
//        assertTrue(filter.records.containsValue(mainRecord));
//    }
//
//    @Test
//    public void testProcessSecondDifferentTrip() {
//        filter.records.put(mainRecord.getType(), mainRecord);
//
//
//        RecordVO input = new RecordVO();
//        input.setUserId("not");
//        input.setTripId("not");
//        input.setType("type");
//        input.setTime(1000L);
//        input.setValue(1.5);
//        input.setSaved(false);
//
//        when(converter.convert(input)).thenReturn(entityRecord);
//
//        filter.process(input);
//
//        assertTrue(filter.records.containsKey(input.getType()));
//        assertTrue(filter.records.containsValue(input));
//
//        verify(recordHelper).save(entityRecord);
//    }
//
//    @Test
//    public void testProcessSecondGreaterDifference() {
//        filter.records.put(mainRecord.getType(), mainRecord);
//
//        RecordEntity secondEntity = new RecordEntity();
//
//        RecordVO input = new RecordVO();
//        input.setUserId("user");
//        input.setTripId("trip");
//        input.setType("type");
//        input.setTime(1200L);
//        input.setValue(5.5);
//        input.setSaved(false);
//
//        when(converter.convert(mainRecord)).thenReturn(entityRecord);
//        when(converter.convert(input)).thenReturn(secondEntity);
//
//        filter.process(input);
//
//        assertTrue(filter.records.containsKey(input.getType()));
//        assertTrue(filter.records.containsValue(input));
//
//        verify(recordHelper).save(entityRecord);
//        verify(recordHelper).save(secondEntity);
//    }
//
//    @Test
//    public void testProcessTimeExceed() {
//        filter.records.put(mainRecord.getType(), mainRecord);
//
//        RecordVO input = new RecordVO();
//        input.setUserId("user");
//        input.setTripId("trip");
//        input.setType("type");
//        input.setTime(3000L);
//        input.setLastSaved(64000L);
//        input.setValue(1.5);
//        input.setSaved(false);
//
//        when(converter.convert(mainRecord)).thenReturn(entityRecord);
//
//        filter.process(input);
//
//        assertTrue(filter.records.containsKey(input.getType()));
//        assertTrue(filter.records.containsValue(input));
//
//        verify(recordHelper).save(entityRecord);
//    }
//
//    @Test
//    public void testProcessRounding() {
//        mainRecord.setLastSaved(1L);
//        mainRecord.setTime(2L);
//        mainRecord.setValue(8000.0);
//
//        filter.records.put(mainRecord.getType(), mainRecord);
//
//        RecordVO input = new RecordVO();
//        input.setUserId("user");
//        input.setTripId("trip");
//        input.setType("type");
//        input.setTime(4L);
//        input.setLastSaved(4L);
//        input.setValue(8300L);
//        input.setSaved(false);
//
//        filter.process(input);
//
//        assertTrue(filter.records.containsKey(input.getType()));
//        assertTrue(filter.records.containsValue(input));
//
//        RecordVO result = filter.records.get(input.getType());
//        assertEquals(8200L, result.getValue(), 0);
//        assertEquals((Long) 1L, result.getLastSaved());
//        assertEquals((Long) 4L, result.getTime());
//
//        verifyNoMoreInteractions(recordHelper);
//    }
//
//    @Test
//    public void testSaveNew() {
//        RecordVO recordVO = new RecordVO();
//        recordVO.setUserId("user");
//        recordVO.setTripId("trip");
//        recordVO.setType("type");
//        recordVO.setTime(1000L);
//        recordVO.setValue(1.5);
//        recordVO.setSaved(false);
//
//        when(converter.convert(recordVO)).thenReturn(entityRecord);
//
//        filter.saveNew(recordVO);
//
//        verify(recordHelper).save(entityRecord);
//    }
//
//    @Test
//    public void testRounding() {
//        double result = filter.round(123456789.5866456464564, 3);
//
//        assertEquals(123456789.587, result);
//    }
}