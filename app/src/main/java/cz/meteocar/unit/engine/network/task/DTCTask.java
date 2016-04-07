package cz.meteocar.unit.engine.network.task;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.network.NetworkException;
import cz.meteocar.unit.engine.network.dto.CreateDiagnosticTroubleCodeRequest;
import cz.meteocar.unit.engine.network.task.converter.DTCEntity2DiagnosticTroubleCodeDtoConverter;
import cz.meteocar.unit.engine.storage.helper.DTCHelper;
import cz.meteocar.unit.engine.storage.model.DTCEntity;
import cz.meteocar.unit.engine.task.AbstractTask;

/**
 * DTCTask sends stored and not yet posted DTC to server and after successful set theirs posted to true.
 */
public class DTCTask extends AbstractTask {

    private DTCEntity2DiagnosticTroubleCodeDtoConverter converter = new DTCEntity2DiagnosticTroubleCodeDtoConverter();
    private NetworkConnector<CreateDiagnosticTroubleCodeRequest, Void> postConnector = new NetworkConnector<>(CreateDiagnosticTroubleCodeRequest.class, Void.class, "dtcs");
    private DTCHelper dao = ServiceManager.getInstance().getDB().getDTCHelper();

    @Override
    public void runTask() {
        while (dao.getNumberOfRecords(false) != 0) {
            List<DTCEntity> records = dao.getRecords(false, 100);

            try {
                postConnector.post(new CreateDiagnosticTroubleCodeRequest(Lists.newArrayList(converter.convertAll(records))));
            } catch (NetworkException e) {
                postNetworkException(e);
                break;
            }

            List<Integer> integers = new ArrayList<>();
            for (DTCEntity recordEntity : records) {
                integers.add(recordEntity.getId());
            }

            dao.updatePosted(integers, true);
        }
    }

}
