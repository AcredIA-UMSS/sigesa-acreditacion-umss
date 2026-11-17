package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.PrimarySurveyBatch;

import java.util.List;
import java.util.UUID;

public interface ListPrimarySurveyBatchesUseCase {

    List<PrimarySurveyBatch> list(UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope);
}
