package cz.meteocar.unit.engine.network.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Create Diagnostic trouble code request transport object.
 */
public class CreateDiagnosticTroubleCodeRequest {

    private List<DiagnosticTroubleCodeDto> records;

    public CreateDiagnosticTroubleCodeRequest(List<DiagnosticTroubleCodeDto> records) {
        this.records = records;
    }

    public List<DiagnosticTroubleCodeDto> getRecords() {
        if (this.records == null) {
            this.records = new ArrayList<>();
        }
        return records;
    }
}
