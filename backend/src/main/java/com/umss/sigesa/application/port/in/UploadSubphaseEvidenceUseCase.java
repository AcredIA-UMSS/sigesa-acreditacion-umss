package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.EvidenceUploadResult;
import com.umss.sigesa.domain.model.SubphaseEvidenceUploadCommand;

public interface UploadSubphaseEvidenceUseCase {

    EvidenceUploadResult upload(SubphaseEvidenceUploadCommand command);
}
