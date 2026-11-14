package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.ImprovementPlanItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImprovementPlanItemPort {

    List<ImprovementPlanItem> findByProcessId(UUID processId);

    Optional<ImprovementPlanItem> findByIdAndProcessId(UUID itemId, UUID processId);

    boolean existsByProcessIdAndSourceIndicatorId(UUID processId, UUID sourceIndicatorId);

    ImprovementPlanItem save(ImprovementPlanItem item);
}
