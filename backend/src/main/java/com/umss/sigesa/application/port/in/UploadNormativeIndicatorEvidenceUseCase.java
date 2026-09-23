package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceUploadCommand;
import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceUploadResult;

public interface UploadNormativeIndicatorEvidenceUseCase {

    NormativeIndicatorEvidenceUploadResult upload(NormativeIndicatorEvidenceUploadCommand command);
}
