package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessType;

import java.util.UUID;

public interface AccreditationProcessRepositoryPort {
    // Firma explícita para la validación de FSD-BR-08
    boolean existsActiveProcessByCareerAndTypeAndPeriod(UUID careerId, ProcessType type, String period);
    AccreditationProcess save(AccreditationProcess process);
}
