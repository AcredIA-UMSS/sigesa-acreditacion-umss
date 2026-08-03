package com.umss.sigesa.application.model.process;

import java.util.List;
import java.util.UUID;

public record ProcessQueryContext(String role, List<UUID> programScope) {
}
