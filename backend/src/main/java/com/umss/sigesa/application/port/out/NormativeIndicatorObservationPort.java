package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.IndicatorObservation;

import java.util.Optional;
import java.util.UUID;

public interface NormativeIndicatorObservationPort {

    Optional<IndicatorObservation> findLatestOpenByIndicatorId(UUID indicatorId);

    IndicatorObservation save(IndicatorObservation observation);
}
