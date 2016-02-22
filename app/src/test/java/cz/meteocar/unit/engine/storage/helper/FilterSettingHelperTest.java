package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Nell on 17.2.2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class FilterSettingHelperTest {

    private static final String TABLE_NAME = "filter_setting";

    @Mock
    private SQLiteDatabase database;

    @Mock
    private DatabaseHelper helper;

    @Mock
    private ContentValues values;

    private FilterSettingHelper filterSettingHelper;

    @Before
    public void setUp() {
        filterSettingHelper = new FilterSettingHelper(helper);
    }

    @Test
    public void testInnerSave() {
        long saveId = 7L;
        when(helper.getWritableDatabase()).thenReturn(database);
        int id = filterSettingHelper.innerSave((int) saveId, values);
        Assert.assertEquals((int) saveId, id);

        verify(database).update(eq(TABLE_NAME), eq(values), anyString(), eq(new String[]{String.valueOf(saveId)}));
    }

    @Test
    public void testInnerUpdate() {
        long saveId = -1L;
        when(helper.getWritableDatabase()).thenReturn(database);
        when(database.insert(TABLE_NAME, null, values)).thenReturn(saveId);
        int id = filterSettingHelper.innerSave(-1, values);
        Assert.assertEquals((int) saveId, id);

        verify(database).insert(TABLE_NAME, null, values);
    }

}