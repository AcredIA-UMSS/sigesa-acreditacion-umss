package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.ProcessType;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface GetProcessUseCase {

    Optional<ProcessDetail> getById(UUID processId);

    record ProcessDetail(
            UUID processId,
            UUID templateId,
            UUID careerId,
            String period,
            ProcessType type,
            ProcessStatus status,
            String taxonomySnapshotVersion,
            LocalDateTime createdAt
    ) {
    }
}
