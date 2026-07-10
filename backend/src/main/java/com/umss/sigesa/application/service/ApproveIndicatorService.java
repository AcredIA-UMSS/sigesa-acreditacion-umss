package com.umss.sigesa.application.service;

import com.umss.sigesa.application.port.in.ApproveIndicatorUseCase;
import com.umss.sigesa.application.port.out.IndicatorCatalogPort;
import com.umss.sigesa.application.port.out.IndicatorStateHistoryPort;
import com.umss.sigesa.domain.exception.ForbiddenProgramScopeException;
import com.umss.sigesa.domain.exception.InvalidIndicatorStateException;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class ApproveIndicatorService implements ApproveIndicatorUseCase {

    private static final Set<String> ALLOWED_ORIGIN_STATES = Set.of("SUBIDO", "SUBSANADO");

    private final IndicatorCatalogPort indicatorCatalog;
    private final IndicatorStateHistoryPort indicatorStateHistory;

    public ApproveIndicatorService(IndicatorCatalogPort indicatorCatalog,
                                   IndicatorStateHistoryPort indicatorStateHistory) {
        this.indicatorCatalog = indicatorCatalog;
        this.indicatorStateHistory = indicatorStateHistory;
    }

    @Override
    @Transactional
    public ApproveIndicatorResult approve(UUID indicatorId, AuthenticatedIdentity identity) {
        if (identity.role() != Role.TD) {
            throw new ForbiddenProgramScopeException("Solo el Técnico DUEA [TD] puede aprobar indicadores.");
        }

        indicatorCatalog.findById(indicatorId)
                .orElseThrow(() -> new IllegalArgumentException("Indicador no encontrado."));

        String currentState = indicatorStateHistory.findLatestState(indicatorId).orElse("PENDIENTE");
        if (!ALLOWED_ORIGIN_STATES.contains(currentState)) {
            throw new InvalidIndicatorStateException(
                    "No se puede aprobar un indicador en estado '" + currentState + "'.");
        }

        indicatorStateHistory.recordTransition(
                indicatorId, currentState, "APROBADO", identity.userId(), identity.role());

        UUID historyId = indicatorStateHistory.findLatestHistoryId(indicatorId)
                .orElseThrow(() -> new IllegalStateException("No se registró el historial de estado."));

        return new ApproveIndicatorResult("APROBADO", historyId, "IndicatorApproved");
    }
}
