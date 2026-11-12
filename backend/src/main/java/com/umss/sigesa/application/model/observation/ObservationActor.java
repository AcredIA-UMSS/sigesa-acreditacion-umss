package com.umss.sigesa.application.model.observation;

import java.util.List;
import java.util.UUID;

public record ObservationActor(UUID userId, String role, List<UUID> programScope) {
}
