package cz.meteocar.unit.engine.convertor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nell on 23.1.2016.
 */
public abstract class AbstractConverter<I, O> {

    public O convert(I input) {
        if (input == null) {
            return null;
        }

        return innerConvert(input);
    }

    protected abstract O innerConvert(I input);

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
