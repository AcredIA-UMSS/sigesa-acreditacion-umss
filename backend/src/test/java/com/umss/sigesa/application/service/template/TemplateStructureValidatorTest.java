package com.umss.sigesa.application.service.template;

import com.umss.sigesa.domain.exception.TemplateStructureIncompleteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TemplateStructureValidatorTest {

    private TemplateStructureValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TemplateStructureValidator();
    }

    @Test
    void shouldAcceptCeubAndArcuSurTypes() {
        assertDoesNotThrow(() -> validator.validateType("CEUB"));
        assertDoesNotThrow(() -> validator.validateType("arcu-sur"));
    }

    @Test
    void shouldRejectBlankOrUnknownTemplateType() {
        assertThrows(TemplateStructureIncompleteException.class, () -> validator.validateType(null));
        assertThrows(TemplateStructureIncompleteException.class, () -> validator.validateType("   "));
        assertThrows(TemplateStructureIncompleteException.class, () -> validator.validateType("ISO-9001"));
    }
}
