package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.EvidenceUploadCommand;
import com.umss.sigesa.domain.model.EvidenceUploadResult;

public interface UploadEvidenceUseCase {

    EvidenceUploadResult upload(EvidenceUploadCommand command);
}
