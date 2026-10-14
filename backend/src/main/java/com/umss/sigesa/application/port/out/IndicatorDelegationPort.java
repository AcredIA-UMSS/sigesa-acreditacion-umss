package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.IndicatorDelegation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IndicatorDelegationPort {

    List<IndicatorDelegation> findByProcessId(UUID processId);

    List<IndicatorDelegation> findByProcessIdAndUserId(UUID processId, UUID userId);

    Optional<IndicatorDelegation> findByIdAndProcessId(UUID delegationId, UUID processId);

    IndicatorDelegation save(IndicatorDelegation delegation);

    void deleteById(UUID delegationId);
}
