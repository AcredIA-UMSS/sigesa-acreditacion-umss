package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.observation.ProcessObservation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessObservationRepositoryPort {

    ProcessObservation save(ProcessObservation observation);

    Optional<ProcessObservation> findById(UUID id);

    List<ProcessObservation> findByProcessId(UUID processId);
}
