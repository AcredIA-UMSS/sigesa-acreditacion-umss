package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.application.port.out.SubphaseWorkflowPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Stub v1.0: sin evidencias ligadas a subfase hasta PR-IMPL-006+.
 * Reemplazar con implementación real vía {@code @ConditionalOnMissingBean} en adaptador futuro.
 */
@Component
public class SubphaseWorkflowStubAdapter implements SubphaseWorkflowPort {

    @Override
    public boolean hasBlockingEvidence(UUID subphaseId) {
        return false;
    }
}
