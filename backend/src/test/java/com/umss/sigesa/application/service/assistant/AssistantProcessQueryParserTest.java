package com.umss.sigesa.application.service.assistant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantProcessQueryParserTest {

    @Test
    void parse_extractsArcuSurTemplateFromCombinedQuery() {
        AssistantProcessQueryParser.ParsedProcessQuery parsed = AssistantProcessQueryParser.parse(
                "ingenieria de sistemas ARCUSUR");

        assertThat(parsed.templateType()).isEqualTo("ARCU-SUR");
        assertThat(parsed.careerQuery()).isEqualTo("ingenieria de sistemas");
    }

    @Test
    void parse_extractsCeubTemplate() {
        AssistantProcessQueryParser.ParsedProcessQuery parsed = AssistantProcessQueryParser.parse(
                "Ingeniería de Sistemas CEUB");

        assertThat(parsed.templateType()).isEqualTo("CEUB");
        assertThat(parsed.careerQuery()).containsIgnoringCase("ingenier");
    }
}
