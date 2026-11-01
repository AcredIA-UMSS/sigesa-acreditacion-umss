package com.umss.sigesa.adapter.out.report;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenPdfDraftingDocumentRendererAdapterTest {

    @Test
    void render_includesInstitutionalLegend() {
        OpenPdfDraftingDocumentRendererAdapter adapter = new OpenPdfDraftingDocumentRendererAdapter();
        byte[] pdf = adapter.render("Plan de Mejoras (borrador institucional)", List.of("Fila 1"));
        assertTrue(pdf.length > 100);
        assertTrue(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1).startsWith("%PDF"));
    }
}
