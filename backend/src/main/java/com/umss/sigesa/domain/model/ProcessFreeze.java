package com.umss.sigesa.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessFreeze {

    private UUID id;
    private UUID processId;
    private ProcessFreezeMode mode;
    private LocalDateTime frozenAt;
    private UUID frozenBy;
    private LocalDateTime liftedAt;
    private UUID liftedBy;

    public boolean isActiveAuditReadonly() {
        return liftedAt == null && mode == ProcessFreezeMode.AUDIT_READONLY;
    }
}
