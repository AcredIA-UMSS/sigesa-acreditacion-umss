package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.in.ApproveIndicatorUseCase;
import com.umss.sigesa.application.port.out.EvidenceControlQueryPort;
import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.ObservationRepositoryPort;
import com.umss.sigesa.adapter.out.persistance.entity.ObservationEntity;
import com.umss.sigesa.domain.exception.ForbiddenRoleException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.InvalidIndicatorStateException;
import com.umss.sigesa.domain.exception.EvidenceNotFoundException;
import com.umss.sigesa.domain.model.Indicator;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.IndicatorStateHistoryEntry;
import com.umss.sigesa.domain.model.Role;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ApproveIndicatorService implements ApproveIndicatorUseCase {

    private final IndicatorRepositoryPort indicatorRepository;
    private final EvidenceControlQueryPort evidenceControlQueryPort;
    private final ObservationRepositoryPort observationRepositoryPort;
    private final NotificationOutboxPort notificationOutbox;

    public ApproveIndicatorService(IndicatorRepositoryPort indicatorRepository,
                                   EvidenceControlQueryPort evidenceControlQueryPort,
                                   ObservationRepositoryPort observationRepositoryPort,
                                   NotificationOutboxPort notificationOutbox) {
        this.indicatorRepository = indicatorRepository;
        this.evidenceControlQueryPort = evidenceControlQueryPort;
        this.observationRepositoryPort = observationRepositoryPort;
        this.notificationOutbox = notificationOutbox;
    }

    @Override
    @Transactional
    public ApproveResult approve(UUID indicatorId, UUID actorId, Role actorRole) {
        if (actorRole != Role.TD) {
            throw new ForbiddenRoleException("Solo el Director Tecnico [TD] puede aprobar indicadores");
        }

        Indicator indicator = indicatorRepository.findById(indicatorId)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));

        IndicatorState currentState = indicatorRepository.getCurrentState(indicatorId);
        if (currentState != IndicatorState.SUBIDO && currentState != IndicatorState.SUBSANADO) {
            throw new InvalidIndicatorStateException("El indicador debe estar en estado SUBIDO o SUBSANADO para ser aprobado");
        }

        // Validar evidencia asociada
        var evidenceOpt = evidenceControlQueryPort.findByIndicatorId(indicatorId);
        if (evidenceOpt.isEmpty() || evidenceOpt.get().evidenceId() == null) {
            throw new EvidenceNotFoundException(indicatorId);
        }

        // Resolver observaciones previas
        List<ObservationEntity> observations = observationRepositoryPort.findByIndicatorId(indicatorId.toString());
        for (ObservationEntity obs : observations) {
            if (!"RESOLVED".equals(obs.getStatus())) {
                obs.setStatus("RESOLVED");
            }
        }
        observationRepositoryPort.saveAll(observations);

        // Cambiar estado a APROBADO
        UUID historyId = UUID.randomUUID();
        IndicatorStateHistoryEntry historyEntry = new IndicatorStateHistoryEntry(
                historyId,
                indicatorId,
                currentState,
                IndicatorState.APROBADO,
                actorId,
                actorRole,
                LocalDateTime.now()
        );
        indicatorRepository.appendStateHistory(historyEntry);

        // Notificar y publicar evento
        notificationOutbox.enqueue("IndicatorApproved", indicator.getProgramId(), Map.of(
                "indicatorId", indicatorId.toString(),
                "phaseId", indicator.getPhaseId() != null ? indicator.getPhaseId().toString() : ""
        ));

        return new ApproveResult(IndicatorState.APROBADO, historyId, "IndicatorApproved");
    }
}
