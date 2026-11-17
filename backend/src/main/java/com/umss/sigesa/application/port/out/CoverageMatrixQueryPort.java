package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.CoverageIndicatorInput;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CoverageMatrixQueryPort {

    List<CoverageIndicatorInput> findIndicatorRows(UUID processId);

    Optional<String> findCareerName(UUID careerId);
}
