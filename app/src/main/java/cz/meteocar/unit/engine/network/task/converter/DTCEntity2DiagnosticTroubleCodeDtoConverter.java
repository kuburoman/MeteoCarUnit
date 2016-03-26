package cz.meteocar.unit.engine.network.task.converter;

import com.google.common.base.Converter;

import cz.meteocar.unit.engine.network.dto.DiagnosticTroubleCodeDto;
import cz.meteocar.unit.engine.storage.model.DTCEntity;

/**
 * Converts between {@link DTCEntity} and {@link DiagnosticTroubleCodeDto}.
 */
public class DTCEntity2DiagnosticTroubleCodeDtoConverter extends Converter<DTCEntity, DiagnosticTroubleCodeDto> {

    @Override
    protected DiagnosticTroubleCodeDto doForward(DTCEntity input) {
        DiagnosticTroubleCodeDto result = new DiagnosticTroubleCodeDto();
        result.setTime(input.getTime());
        result.setTripHashcode(input.getTripId());
        result.setCode(input.getDtcCode());
        return result;
    }

    @Override
    protected DTCEntity doBackward(DiagnosticTroubleCodeDto input) {
        return null;
    }
}
