package cz.meteocar.unit.engine.convertor;

/**
 * Created by Nell on 23.1.2016.
 */
public abstract class AbstractConverter<I, O> {

    public O convert(I input){
        if(input == null){
            return null;
        }

        return innerConvert(input);
    }

    protected abstract O innerConvert(I input);
}
