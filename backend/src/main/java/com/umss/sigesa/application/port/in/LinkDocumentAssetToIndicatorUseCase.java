package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.LinkDocumentAssetToIndicatorCommand;
import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceUploadResult;

public interface LinkDocumentAssetToIndicatorUseCase {

    NormativeIndicatorEvidenceUploadResult link(LinkDocumentAssetToIndicatorCommand command);
}
