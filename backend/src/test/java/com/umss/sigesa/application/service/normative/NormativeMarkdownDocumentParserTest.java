package com.umss.sigesa.application.service.normative;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NormativeMarkdownDocumentParserTest {

    private final NormativeMarkdownDocumentParser parser = new NormativeMarkdownDocumentParser();

    @Test
    void parse_readsFrontMatterAndBody() {
        String markdown = """
                ---
                title: "6.7. Proceso de autoevaluación"
                template_type: ARCU-SUR
                phase_name: "Procedimientos del Sistema ARCU-SUR"
                subphase_name: "Proceso de autoevaluación"
                ---

                # 6.7. Proceso de autoevaluación

                El proceso de autoevaluación requiere un conjunto de acciones.
                """;

        var document = parser.parse(markdown).orElseThrow();

        assertThat(document.title()).isEqualTo("6.7. Proceso de autoevaluación");
        assertThat(document.templateType()).isEqualTo("ARCU-SUR");
        assertThat(document.phaseName()).isEqualTo("Procedimientos del Sistema ARCU-SUR");
        assertThat(document.subphaseName()).isEqualTo("Proceso de autoevaluación");
        assertThat(document.bodyText()).contains("conjunto de acciones");
        assertThat(document.bodyText()).doesNotContain("template_type");
    }

    @Test
    void parse_rejectsMissingFrontMatter() {
        assertThat(parser.parse("# Solo cuerpo")).isEmpty();
        assertThat(parser.parse("")).isEmpty();
    }
}
