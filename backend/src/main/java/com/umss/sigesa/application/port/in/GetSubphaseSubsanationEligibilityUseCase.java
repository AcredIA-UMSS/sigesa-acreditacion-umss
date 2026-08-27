package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.SubphaseSubsanationEligibility;

import java.util.List;
import java.util.UUID;

public interface GetSubphaseSubsanationEligibilityUseCase {

    SubphaseSubsanationEligibility get(UUID subphaseId, UUID userId, List<String> roles);
}
