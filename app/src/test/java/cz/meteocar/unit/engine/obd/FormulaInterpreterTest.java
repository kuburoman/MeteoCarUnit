package cz.meteocar.unit.engine.obd;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * Test for {@link FormulaInterpreter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FormulaInterpreterTest {

    @Test
    public void speedTest(){
        FormulaInterpreter interpreter = new FormulaInterpreter("A");

        assertTrue(interpreter.isSyntaxOK());
        assertEquals(255.0, interpreter.interpretString("41 0D FF"), 0);
    }

    @Test
    public void temperatureTest(){
        FormulaInterpreter interpreter = new FormulaInterpreter("A - 40");

        assertTrue(interpreter.isSyntaxOK());
        assertEquals(215.0, interpreter.interpretString("41 05 FF"), 0);
    }

    @Test
    public void rpmTest(){
        FormulaInterpreter interpreter = new FormulaInterpreter("((256 * A) + B) / 4");

        assertTrue(interpreter.isSyntaxOK());
        assertEquals(960.0, interpreter.interpretString("41 0C 0F 00"), 0);
    }

    @Test
    public void airflowTest(){
        FormulaInterpreter interpreter = new FormulaInterpreter("((256 * A) + B) / 100");

        assertTrue(interpreter.isSyntaxOK());
        assertEquals(40.0, interpreter.interpretString("41 10 0F A0"), 0);
    }

    @Test
    public void throttleTest(){
        FormulaInterpreter interpreter = new FormulaInterpreter("(100 * A) / 255");

        assertTrue(interpreter.isSyntaxOK());
        assertEquals(100.0, interpreter.interpretString("41 11 FF"), 0);
    }

}