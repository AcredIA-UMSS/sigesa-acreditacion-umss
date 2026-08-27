package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.SubphaseObservation;
import com.umss.sigesa.domain.model.SubphaseObservationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubphaseObservationPort {

    SubphaseObservation save(SubphaseObservation observation);

    List<SubphaseObservation> findBySubphaseId(UUID subphaseId);

    Optional<SubphaseObservation> findLatestOpenBySubphaseId(UUID subphaseId);

    SubphaseObservation resolve(UUID observationId, UUID resolvedVersionId);
}
