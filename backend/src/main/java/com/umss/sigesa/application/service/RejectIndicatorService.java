package com.umss.sigesa.application.service;

import com.umss.sigesa.application.port.in.RejectIndicatorUseCase;
import com.umss.sigesa.application.port.out.DashboardQueryPort;
import com.umss.sigesa.application.port.out.EvidenceRepositoryPort;
import com.umss.sigesa.application.port.out.IndicatorCatalogPort;
import com.umss.sigesa.application.port.out.IndicatorStateHistoryPort;
import com.umss.sigesa.application.port.out.ObservationRepositoryPort;
import com.umss.sigesa.domain.exception.ForbiddenProgramScopeException;
import com.umss.sigesa.domain.exception.InvalidIndicatorStateException;
import com.umss.sigesa.domain.exception.JustificationRequiredException;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
public class RejectIndicatorService implements RejectIndicatorUseCase {

    private static final Set<String> ALLOWED_ORIGIN_STATES = Set.of("SUBIDO", "SUBSANADO");

    private final IndicatorCatalogPort indicatorCatalog;
    private final IndicatorStateHistoryPort indicatorStateHistory;
    private final ObservationRepositoryPort observationRepository;
    private final DashboardQueryPort dashboardQueryPort;
    private final EvidenceRepositoryPort evidenceRepository;

    public RejectIndicatorService(IndicatorCatalogPort indicatorCatalog,
                                  IndicatorStateHistoryPort indicatorStateHistory,
                                  ObservationRepositoryPort observationRepository,
                                  DashboardQueryPort dashboardQueryPort,
                                  EvidenceRepositoryPort evidenceRepository) {
        this.indicatorCatalog = indicatorCatalog;
        this.indicatorStateHistory = indicatorStateHistory;
        this.observationRepository = observationRepository;
        this.dashboardQueryPort = dashboardQueryPort;
        this.evidenceRepository = evidenceRepository;
    }

    @Override
    @Transactional
    public RejectIndicatorResult reject(UUID indicatorId, String justification, AuthenticatedIdentity identity) {
        if (identity.role() != Role.TD) {
            throw new ForbiddenProgramScopeException("Solo el Técnico DUEA [TD] puede observar indicadores.");
        }

        if (justification == null || justification.trim().length() < 20) {
            throw new JustificationRequiredException("La justificación debe tener al menos 20 caracteres.");
        }

        IndicatorCatalogPort.IndicatorEntry indicator = indicatorCatalog.findById(indicatorId)
                .orElseThrow(() -> new IllegalArgumentException("Indicador no encontrado."));

        String currentState = indicatorStateHistory.findLatestState(indicatorId).orElse("PENDIENTE");
        if (!ALLOWED_ORIGIN_STATES.contains(currentState)) {
            throw new InvalidIndicatorStateException(
                    "No se puede observar un indicador en estado '" + currentState + "'.");
        }

        UUID evidenceVersionId = evidenceRepository.findByIndicatorId(indicatorId)
                .map(Evidence::getLatestVersionId)
                .orElseThrow(() -> new IllegalStateException("No se encontró evidencia para el indicador a observar."));

        String observationId = UUID.randomUUID().toString();
        LocalDate today = LocalDate.now();

        observationRepository.savePendingObservation(new ObservationRepositoryPort.PendingObservation(
                observationId,
                indicator.programId(),
                indicator.id().toString(),
                indicator.code(),
                indicator.title(),
                justification.trim(),
                today,
                today.plusDays(14),
                indicator.phaseId(),
                "PENDING_REMEDIATION",
                "/evidencias/" + indicator.id() + "/subsanar",
                evidenceVersionId,
                identity.userId(),
                identity.role().name()
        ));

        dashboardQueryPort.updateDashboardMetrics(indicator.programId(), 0, 1, 1);

        indicatorStateHistory.recordTransition(
                indicatorId, currentState, "OBSERVADO", identity.userId(), identity.role());

        UUID historyId = indicatorStateHistory.findLatestHistoryId(indicatorId)
                .orElseThrow(() -> new IllegalStateException("No se registró el historial de estado."));

        return new RejectIndicatorResult("OBSERVADO", observationId, historyId);
    }
}
