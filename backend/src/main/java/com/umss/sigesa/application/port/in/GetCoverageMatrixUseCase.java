package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.CoverageMatrix;

import java.util.List;
import java.util.UUID;

public interface GetCoverageMatrixUseCase {

    CoverageMatrix get(UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope);

    byte[] renderPdf(UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope);
}
