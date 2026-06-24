package com.umss.sigesa.adapter.in.web.dto;

import com.umss.sigesa.domain.model.ProcessStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProcessResponse(
        UUID processId,
        ProcessStatus status,
        String taxonomySnapshotVersion,
        LocalDateTime createdAt
) {}
