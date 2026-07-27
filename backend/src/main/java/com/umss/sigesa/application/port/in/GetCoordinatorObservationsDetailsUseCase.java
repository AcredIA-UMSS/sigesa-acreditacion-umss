package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ObservationSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GetCoordinatorObservationsDetailsUseCase {
    Page<ObservationSummary> getObservationsDetails(UUID programId, Integer phaseId, String status, Pageable pageable);
}
