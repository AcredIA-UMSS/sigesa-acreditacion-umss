package com.umss.sigesa.e2e;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * E2E placeholder — excluido de {@code ./mvnw test} (paquete {@code e2e} en Surefire).
 * Flujo operativo: {@code scripts/run_e2e_docker.sh}.
 */
@Tag("e2e")
@Disabled("E2E via Docker Compose: scripts/run_e2e_docker.sh")
class ReportsE2ETest {

    @Test
    void exportFlow_completes() {
        // Implementación Testcontainers pendiente (requiere @DynamicPropertySource + Docker).
    }
}
