package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.EvidenceSubsanationResult;
import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceSubsanationCommand;

public interface SubsanateNormativeIndicatorEvidenceUseCase {

    EvidenceSubsanationResult subsanate(NormativeIndicatorEvidenceSubsanationCommand command);
}
