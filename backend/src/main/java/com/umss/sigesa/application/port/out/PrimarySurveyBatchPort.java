package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.PrimarySurveyBatch;

import java.util.List;
import java.util.UUID;

public interface PrimarySurveyBatchPort {

    PrimarySurveyBatch save(PrimarySurveyBatch batch);

    List<PrimarySurveyBatch> findByProcessId(UUID processId);

    long countByProcessId(UUID processId);
}
