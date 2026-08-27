package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.SubphaseRejectResult;

import java.util.UUID;

public interface RejectSubphaseIndicatorUseCase {

    SubphaseRejectResult reject(UUID subphaseId, String justification, UUID actorId, String actorRole);
}
