package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceItem;

import java.util.List;
import java.util.UUID;

public interface ListNormativeIndicatorEvidencesUseCase {

    List<NormativeIndicatorEvidenceItem> list(UUID indicatorId, UUID requesterId, List<String> roles);
}
