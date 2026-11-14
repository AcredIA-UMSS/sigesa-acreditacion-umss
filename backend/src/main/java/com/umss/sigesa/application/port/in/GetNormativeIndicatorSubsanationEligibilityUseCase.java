package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.NormativeIndicatorSubsanationEligibility;

import java.util.List;
import java.util.UUID;

public interface GetNormativeIndicatorSubsanationEligibilityUseCase {

    NormativeIndicatorSubsanationEligibility get(UUID indicatorId, UUID userId, List<String> roles);
}
