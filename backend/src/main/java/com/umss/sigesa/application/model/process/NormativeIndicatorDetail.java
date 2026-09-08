package com.umss.sigesa.application.model.process;

import com.umss.sigesa.domain.model.NormativeIndicator;

import java.util.List;
import java.util.UUID;

public record NormativeIndicatorDetail(
        UUID processId,
        UUID careerId,
        UUID level1Id,
        String level1Name,
        NormativeIndicator indicator,
        List<String> normativePath
) {
}
