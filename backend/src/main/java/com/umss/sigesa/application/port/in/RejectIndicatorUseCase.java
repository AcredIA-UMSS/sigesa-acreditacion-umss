package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.IndicatorWorkflowResult;

import java.util.UUID;

public interface RejectIndicatorUseCase {

    IndicatorWorkflowResult reject(UUID indicatorId, String justification, UUID actorId, String actorRole);
}
