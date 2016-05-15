package cz.meteocar.unit.engine.convertor;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract converter.
 */
public abstract class AbstractConverter<I, O> {

    /**
     * From one class to another.
     */
    public O convert(I input) {
        if (input == null) {
            return null;
        }

        return innerConvert(input);
    }

    protected abstract O innerConvert(I input);

    /**
     * Converts list to list.
     */
    public List<O> convertList(List<I> inputList) {
        ArrayList<O> outputList = new ArrayList<>();
        if (inputList == null) {
            return outputList;
        }
        for (I input : inputList) {
            outputList.add(convert(input));
        }
        return outputList;
    }
}
