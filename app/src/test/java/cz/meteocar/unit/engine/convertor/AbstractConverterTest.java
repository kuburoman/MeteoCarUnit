package cz.meteocar.unit.engine.convertor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * Test for {@link AbstractConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractConverterTest {

    AbstractConverter<Object, Object> converter;

    @Before
    public void setUp() {
        converter = Mockito.mock(AbstractConverter.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    public void testConvertNull() {
        assertNull(converter.convert(null));
    }

    @Test
    public void testConvertSuccessful() {
        doReturn(new Object()).when(converter).innerConvert(any(Object.class));
        assertNotNull(converter.convert(new Object()));
    }

    @Test
    public void testConvertListNull() {
        List<Object> objects = converter.convertList(null);
        assertNotNull(objects);
        assertTrue(objects.isEmpty());
    }


    @Test
    public void testConvertListSuccessful() {
        doReturn(new Object()).when(converter).innerConvert(any(Object.class));
        List<Object> input = new ArrayList<>();
        input.add(new Object());
        input.add(new Object());

        List<Object> objects = converter.convertList(input);
        assertNotNull(objects);
        assertEquals(2, objects.size());
    }
}