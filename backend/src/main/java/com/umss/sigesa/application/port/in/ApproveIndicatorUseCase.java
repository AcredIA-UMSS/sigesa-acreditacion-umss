package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.IndicatorWorkflowResult;

import java.util.UUID;

public interface ApproveIndicatorUseCase {

    IndicatorWorkflowResult approve(UUID indicatorId, UUID actorId, String actorRole);
}
