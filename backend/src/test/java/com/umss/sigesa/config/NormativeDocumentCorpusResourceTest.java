package com.umss.sigesa.config;

import com.umss.sigesa.application.service.normative.NormativeMarkdownDocumentParser;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class NormativeDocumentCorpusResourceTest {

    @Test
    void arcuSurClasspathChunksAreParseable() throws Exception {
        var resolver = new PathMatchingResourcePatternResolver();
        Resource[] files = resolver.getResources(NormativeDocumentSeedLoader.ARCU_SUR_CORPUS_PATTERN);
        var parser = new NormativeMarkdownDocumentParser();

        assertThat(files).hasSizeGreaterThanOrEqualTo(30);

        for (Resource file : files) {
            String markdown = new String(file.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            var parsed = parser.parse(markdown);
            assertThat(parsed)
                    .as(file.getFilename())
                    .isPresent();
            assertThat(parsed.get().templateType()).isEqualTo("ARCU-SUR");
            assertThat(parsed.get().bodyText()).isNotBlank();
        }
    }
}
