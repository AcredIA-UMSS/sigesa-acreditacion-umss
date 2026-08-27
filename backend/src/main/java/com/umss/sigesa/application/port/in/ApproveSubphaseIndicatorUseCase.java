package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.SubphaseApproveResult;

import java.util.UUID;

public interface ApproveSubphaseIndicatorUseCase {

    SubphaseApproveResult approve(UUID subphaseId, UUID actorId, String actorRole);
}
