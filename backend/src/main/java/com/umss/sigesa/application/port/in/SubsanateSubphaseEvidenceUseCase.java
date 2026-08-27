package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.EvidenceSubsanationResult;
import com.umss.sigesa.domain.model.SubphaseEvidenceSubsanationCommand;

public interface SubsanateSubphaseEvidenceUseCase {

    EvidenceSubsanationResult subsanate(SubphaseEvidenceSubsanationCommand command);
}
