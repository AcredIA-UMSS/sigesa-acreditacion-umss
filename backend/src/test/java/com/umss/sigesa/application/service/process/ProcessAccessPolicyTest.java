package com.umss.sigesa.application.service.process;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessAccessPolicyTest {

    private final UUID careerA = UUID.randomUUID();
    private final UUID careerB = UUID.randomUUID();

    @Test
    void jdCanAccessAnyCareer() {
        assertTrue(ProcessAccessPolicy.canAccess("JD", careerB, List.of(careerA)));
    }

    @Test
    void tdCanAccessAnyCareer() {
        assertTrue(ProcessAccessPolicy.canAccess("TD", careerB, List.of(careerA)));
    }

    @Test
    void ccCanAccessAssignedCareer() {
        assertTrue(ProcessAccessPolicy.canAccess("CC", careerA, List.of(careerA)));
    }

    @Test
    void ccCannotAccessForeignCareer() {
        assertFalse(ProcessAccessPolicy.canAccess("CC", careerB, List.of(careerA)));
    }

    @Test
    void ccWithEmptyScopeCannotAccess() {
        assertFalse(ProcessAccessPolicy.canAccess("CC", careerA, List.of()));
    }
}
