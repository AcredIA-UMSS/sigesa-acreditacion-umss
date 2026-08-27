package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.SubphaseObservation;

import java.util.UUID;

public interface AddSubphaseObservationUseCase {

    SubphaseObservation add(UUID subphaseId, String body, UUID authorId, String authorRole);
}
