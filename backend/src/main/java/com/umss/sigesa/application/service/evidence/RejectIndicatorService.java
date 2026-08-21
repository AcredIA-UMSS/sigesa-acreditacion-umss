package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.in.RejectIndicatorUseCase;
import com.umss.sigesa.application.port.out.EvidenceControlQueryPort;
import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.ObservationRepositoryPort;
import com.umss.sigesa.adapter.out.persistance.entity.ObservationEntity;
import com.umss.sigesa.domain.exception.ForbiddenRoleException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.InvalidIndicatorStateException;
import com.umss.sigesa.domain.exception.JustificationRequiredException;
import com.umss.sigesa.domain.exception.EvidenceNotFoundException;
import com.umss.sigesa.domain.model.Indicator;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.IndicatorStateHistoryEntry;
import com.umss.sigesa.domain.model.Role;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class RejectIndicatorService implements RejectIndicatorUseCase {

    private final IndicatorRepositoryPort indicatorRepository;
    private final EvidenceControlQueryPort evidenceControlQueryPort;
    private final ObservationRepositoryPort observationRepositoryPort;
    private final NotificationOutboxPort notificationOutbox;

    public RejectIndicatorService(IndicatorRepositoryPort indicatorRepository,
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
    public RejectResult reject(UUID indicatorId, String justification, UUID actorId, Role actorRole) {
        if (actorRole != Role.TD) {
            throw new ForbiddenRoleException("Solo el Director Tecnico [TD] puede rechazar indicadores");
        }

        if (justification == null || justification.trim().length() < 20) {
            throw new JustificationRequiredException("La justificación debe tener al menos 20 caracteres");
        }

        Indicator indicator = indicatorRepository.findById(indicatorId)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));

        IndicatorState currentState = indicatorRepository.getCurrentState(indicatorId);
        if (currentState != IndicatorState.SUBIDO && currentState != IndicatorState.SUBSANADO) {
            throw new InvalidIndicatorStateException("El indicador debe estar en estado SUBIDO o SUBSANADO para ser rechazado");
        }

        // Validar evidencia asociada
        var evidenceOpt = evidenceControlQueryPort.findByIndicatorId(indicatorId);
        if (evidenceOpt.isEmpty() || evidenceOpt.get().evidenceId() == null) {
            throw new EvidenceNotFoundException(indicatorId);
        }

        // Crear observación
        String obsId = "OBS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ObservationEntity obs = new ObservationEntity();
        obs.setObservationId(obsId);
        obs.setProgramId(indicator.getProgramId());
        obs.setIndicatorId(indicatorId.toString());
        obs.setIndicatorCode(evidenceOpt.get().indicatorId() != null ? indicatorId.toString().substring(0, 8) : "IND");
        obs.setIndicatorTitle(indicatorId.toString());
        obs.setDescription(justification);
        obs.setIssueDate(LocalDate.now());
        obs.setDueDate(LocalDate.now().plusDays(10)); // Default due date 10 days
        obs.setStatus("PENDIENTE_SUBSANACION");
        obs.setRemediationUrl("/coordinator/evidences/" + indicatorId + "/subsanar");
        observationRepositoryPort.save(obs);

        // Cambiar estado a OBSERVADO
        UUID historyId = UUID.randomUUID();
        IndicatorStateHistoryEntry historyEntry = new IndicatorStateHistoryEntry(
                historyId,
                indicatorId,
                currentState,
                IndicatorState.OBSERVADO,
                actorId,
                actorRole,
                LocalDateTime.now()
        );
        indicatorRepository.appendStateHistory(historyEntry);

        // Notificar y publicar evento
        notificationOutbox.enqueue("IndicatorRejected", indicator.getProgramId(), Map.of(
                "indicatorId", indicatorId.toString(),
                "observationId", obsId
        ));

        return new RejectResult(IndicatorState.OBSERVADO, obsId, historyId);
    }
}
