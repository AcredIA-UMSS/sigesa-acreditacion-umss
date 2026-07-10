package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.ProcessType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ListProcessesUseCase {

    List<ProcessSummary> list(ProcessStatus status, UUID careerId, String period);

    record ProcessSummary(
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
