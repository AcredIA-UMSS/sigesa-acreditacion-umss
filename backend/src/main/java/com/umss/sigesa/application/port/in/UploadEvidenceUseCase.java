package com.umss.sigesa.application.port.in;

import com.umss.sigesa.adapter.in.web.dto.EvidenceResponse;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import java.util.UUID;

public interface UploadEvidenceUseCase {
    EvidenceResponse upload(UUID indicatorId, UUID criterionId, String description, String filename, byte[] fileBytes, String contentType, AuthenticatedIdentity identity);
}
