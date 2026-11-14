package com.umss.sigesa.application.service.observation;

import com.umss.sigesa.application.model.observation.ObservationActor;
import com.umss.sigesa.application.model.observation.ObservationDraft;
import com.umss.sigesa.application.model.observation.ObservationFilter;
import com.umss.sigesa.application.port.in.ProcessObservationUseCase;
import com.umss.sigesa.application.port.out.EvidenceLookupPort;
import com.umss.sigesa.application.port.out.ProcessObservationRepositoryPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.service.process.ProcessAccessPolicy;
import com.umss.sigesa.domain.exception.EvidenceNotFoundException;
import com.umss.sigesa.domain.exception.EvidenceNotInProcessException;
import com.umss.sigesa.domain.exception.ForbiddenObservationRoleException;
import com.umss.sigesa.domain.exception.ObservationNotFoundException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.observation.ProcessObservation;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ProcessObservationService implements ProcessObservationUseCase {

    private final ProcessQueryPort processQueryPort;
    private final EvidenceLookupPort evidenceLookupPort;
    private final ProcessObservationRepositoryPort observationRepository;
    private final Clock clock;

    public ProcessObservationService(ProcessQueryPort processQueryPort,
                                     EvidenceLookupPort evidenceLookupPort,
                                     ProcessObservationRepositoryPort observationRepository,
                                     Clock clock) {
        this.processQueryPort = processQueryPort;
        this.evidenceLookupPort = evidenceLookupPort;
        this.observationRepository = observationRepository;
        this.clock = clock;
    }

    @Override
    public List<EvidenceLookupPort.EvidenceRef> listEvidences(UUID processId, ObservationActor actor) {
        AccreditationProcess process = visibleProcess(processId, actor);
        return evidenceLookupPort.listByProgramId(process.getCareerId());
    }

    @Override
    public List<ProcessObservation> list(UUID processId, ObservationFilter filter, ObservationActor actor) {
        visibleProcess(processId, actor);
        return observationRepository.findByProcessId(processId).stream()
                .filter(item -> matches(item, filter))
                .toList();
    }

    @Override
    public ProcessObservation get(UUID processId, UUID observationId, ObservationActor actor) {
        visibleProcess(processId, actor);
        return load(processId, observationId);
    }

    @Override
    public ProcessObservation create(UUID processId, ObservationDraft draft, ObservationActor actor) {
        requireRole(actor, "TD", "Solo el técnico registra observaciones");
        AccreditationProcess process = visibleProcess(processId, actor);
        EvidenceLookupPort.EvidenceRef evidence = evidenceInProcess(draft.evidenceId(), process);
        Instant now = clock.instant();
        ProcessObservation created = ProcessObservation.draft(
                UUID.randomUUID(),
                processId,
                evidence.evidenceId(),
                evidence.indicatorId(),
                draft.description(),
                draft.kind(),
                draft.priority(),
                draft.compliance(),
                draft.recommendation(),
                actor.userId(),
                now);
        return observationRepository.save(created);
    }

    @Override
    public ProcessObservation update(UUID processId, UUID observationId, ObservationDraft draft, ObservationActor actor) {
        requireRole(actor, "TD", "Solo el técnico edita observaciones");
        AccreditationProcess process = visibleProcess(processId, actor);
        ProcessObservation current = load(processId, observationId);
        EvidenceLookupPort.EvidenceRef evidence = evidenceInProcess(draft.evidenceId(), process);
        current.updateDraft(
                evidence.evidenceId(),
                evidence.indicatorId(),
                draft.description(),
                draft.kind(),
                draft.priority(),
                draft.compliance(),
                draft.recommendation(),
                actor.userId(),
                clock.instant());
        return observationRepository.save(current);
    }

    @Override
    public ProcessObservation submit(UUID processId, UUID observationId, ObservationActor actor) {
        requireRole(actor, "TD", "Solo el técnico envía la retroalimentación");
        visibleProcess(processId, actor);
        ProcessObservation current = load(processId, observationId);
        if (!current.getAuthorId().equals(actor.userId())) {
            throw new ForbiddenObservationRoleException("Solo el autor puede enviar la observación");
        }
        current.submit(clock.instant());
        return observationRepository.save(current);
    }

    @Override
    public ProcessObservation startReview(UUID processId, UUID observationId, ObservationActor actor) {
        requireRole(actor, "CC", "Solo el coordinador revisa observaciones");
        visibleProcess(processId, actor);
        ProcessObservation current = load(processId, observationId);
        current.startReview(clock.instant());
        return observationRepository.save(current);
    }

    @Override
    public ProcessObservation reply(UUID processId, UUID observationId, String body, ObservationActor actor) {
        requireRole(actor, "CC", "Solo el coordinador responde observaciones");
        visibleProcess(processId, actor);
        ProcessObservation current = load(processId, observationId);
        current.reply(actor.userId(), actor.role(), body, clock.instant());
        return observationRepository.save(current);
    }

    @Override
    public ProcessObservation attend(UUID processId, UUID observationId, String body, ObservationActor actor) {
        requireRole(actor, "CC", "Solo el coordinador marca la observación como atendida");
        visibleProcess(processId, actor);
        ProcessObservation current = load(processId, observationId);
        current.attend(actor.userId(), actor.role(), body, clock.instant());
        return observationRepository.save(current);
    }

    @Override
    public ProcessObservation close(UUID processId, UUID observationId, String body, ObservationActor actor) {
        requireRole(actor, "TD", "Solo el técnico cierra la observación");
        visibleProcess(processId, actor);
        ProcessObservation current = load(processId, observationId);
        current.close(actor.userId(), actor.role(), body, clock.instant());
        return observationRepository.save(current);
    }

    private AccreditationProcess visibleProcess(UUID processId, ObservationActor actor) {
        if (!"TD".equals(actor.role()) && !"CC".equals(actor.role())) {
            throw new ForbiddenObservationRoleException("El rol no consulta observaciones de proceso");
        }
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        ProcessAccessPolicy.assertCanAccess(actor.role(), process.getCareerId(), actor.programScope(), processId);
        return process;
    }

    private EvidenceLookupPort.EvidenceRef evidenceInProcess(UUID evidenceId, AccreditationProcess process) {
        EvidenceLookupPort.EvidenceRef evidence = evidenceLookupPort.findById(evidenceId)
                .orElseThrow(() -> new EvidenceNotFoundException(evidenceId));
        if (!process.getCareerId().equals(evidence.programId())) {
            throw new EvidenceNotInProcessException(evidenceId);
        }
        return evidence;
    }

    private ProcessObservation load(UUID processId, UUID observationId) {
        ProcessObservation observation = observationRepository.findById(observationId)
                .orElseThrow(() -> new ObservationNotFoundException(observationId));
        if (!processId.equals(observation.getProcessId())) {
            throw new ObservationNotFoundException(observationId);
        }
        return observation;
    }

    private static void requireRole(ObservationActor actor, String role, String message) {
        if (!role.equals(actor.role())) {
            throw new ForbiddenObservationRoleException(message);
        }
    }

    private static boolean matches(ProcessObservation item, ObservationFilter filter) {
        if (filter == null) {
            return true;
        }
        if (filter.status() != null && item.getStatus() != filter.status()) {
            return false;
        }
        if (filter.priority() != null && item.getPriority() != filter.priority()) {
            return false;
        }
        if (filter.evidenceId() != null && !filter.evidenceId().equals(item.getEvidenceId())) {
            return false;
        }
        return filter.authorId() == null || filter.authorId().equals(item.getAuthorId());
    }
}
