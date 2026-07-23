package com.umss.sigesa.adapter.in.web.dto;

import java.util.UUID;

public record EvidenceResponse(
    UUID evidenceId,
    int version,
    String contentHash,
    String event
) {}
