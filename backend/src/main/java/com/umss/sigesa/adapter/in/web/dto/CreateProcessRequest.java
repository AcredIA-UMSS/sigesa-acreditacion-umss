package com.umss.sigesa.adapter.in.web.dto;

import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.ProcessType;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateProcessRequest(
        UUID templateId,
        UUID careerId,
        String period,
        ProcessType type
) {}

