package com.umss.sigesa.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("AuditActorEmail")
class AuditActorEmailTest {

    @Test
    @DisplayName("conserva un correo institucional normalizado")
    void keepsInstitutionalEmail() {
        assertEquals("cc@umss.edu.bo", AuditActorEmail.snapshot("  CC@UMSS.EDU.BO "));
    }

    @Test
    @DisplayName("descarta dominio ajeno, texto libre y valores demasiado largos")
    void dropsAnythingThatIsNotAnInstitutionalEmail() {
        assertNull(AuditActorEmail.snapshot("user@gmail.com"));
        assertNull(AuditActorEmail.snapshot("s3cret-password"));
        assertNull(AuditActorEmail.snapshot(null));
        assertNull(AuditActorEmail.snapshot("a".repeat(140) + "@umss.edu.bo"));
    }
}
