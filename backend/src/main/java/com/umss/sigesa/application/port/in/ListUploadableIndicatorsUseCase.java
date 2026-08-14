package com.umss.sigesa.application.port.in;

import com.umss.sigesa.application.model.evidence.UploadableIndicator;

import java.util.List;
import java.util.UUID;

public interface ListUploadableIndicatorsUseCase {

    /**
     * Indicadores en PENDIENTE/OBSERVADO dentro del alcance de carrera del [CC].
     */
    List<UploadableIndicator> listForCoordinator(List<UUID> programScope);
}
