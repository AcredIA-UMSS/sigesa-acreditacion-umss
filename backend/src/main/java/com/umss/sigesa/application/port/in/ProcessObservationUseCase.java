package com.umss.sigesa.application.port.in;

import com.umss.sigesa.application.model.observation.ObservationActor;
import com.umss.sigesa.application.model.observation.ObservationDraft;
import com.umss.sigesa.application.model.observation.ObservationFilter;
import com.umss.sigesa.application.port.out.EvidenceLookupPort;
import com.umss.sigesa.domain.model.observation.ProcessObservation;

import java.util.List;
import java.util.UUID;

public interface ProcessObservationUseCase {

    List<EvidenceLookupPort.EvidenceRef> listEvidences(UUID processId, ObservationActor actor);

    List<ProcessObservation> list(UUID processId, ObservationFilter filter, ObservationActor actor);

    ProcessObservation get(UUID processId, UUID observationId, ObservationActor actor);

    ProcessObservation create(UUID processId, ObservationDraft draft, ObservationActor actor);

    ProcessObservation update(UUID processId, UUID observationId, ObservationDraft draft, ObservationActor actor);

    ProcessObservation submit(UUID processId, UUID observationId, ObservationActor actor);

    ProcessObservation startReview(UUID processId, UUID observationId, ObservationActor actor);

    ProcessObservation reply(UUID processId, UUID observationId, String body, ObservationActor actor);

    ProcessObservation attend(UUID processId, UUID observationId, String body, ObservationActor actor);

    ProcessObservation close(UUID processId, UUID observationId, String body, ObservationActor actor);
}
