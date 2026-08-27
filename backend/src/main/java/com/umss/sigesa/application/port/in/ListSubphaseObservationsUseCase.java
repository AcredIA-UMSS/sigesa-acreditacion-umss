package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.SubphaseObservation;

import java.util.List;
import java.util.UUID;

public interface ListSubphaseObservationsUseCase {

    List<SubphaseObservation> list(UUID subphaseId, UUID requesterId, List<String> roles);
}
