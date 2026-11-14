package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.entity.NormativeDocumentJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataNormativeDocumentRepository;
import com.umss.sigesa.application.service.normative.NormativeMarkdownDocumentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Corpus normativo para RAG: manual ARCU-SUR 2023 (classpath) + seeds CEUB/FAQ en no-prod.
 */
@Component
@Order(95)
public class NormativeDocumentSeedLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(NormativeDocumentSeedLoader.class);
    static final String ARCU_SUR_CORPUS_PATTERN = "classpath:normative-corpus/arcu-sur-2023/*.md";
    static final String ARCU_SUR_SOURCE_PREFIX = "https://arcusur.org/manual-procedimientos-2023#";
    private static final String LEGACY_ARCU_PLACEHOLDER = "https://duea.umss.edu.bo/normativa/arcu-sur/";

    private final SpringDataNormativeDocumentRepository repository;
    private final Environment environment;
    private final NormativeMarkdownDocumentParser parser = new NormativeMarkdownDocumentParser();
    private final PathMatchingResourcePatternResolver resources = new PathMatchingResourcePatternResolver();

    public NormativeDocumentSeedLoader(SpringDataNormativeDocumentRepository repository,
                                       Environment environment) {
        this.repository = repository;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        removeLegacyArcuSurPlaceholders();
        int upserted = loadArcuSurCorpus();
        log.info("RAG corpus ARCU-SUR 2023: {} chunks upserted", upserted);

        if (!environment.acceptsProfiles(Profiles.of("prod"))) {
            if (repository.countByTemplateType("CEUB") == 0) {
                seedCeubDocuments();
            }
            if (repository.countByTemplateType("GENERAL") == 0) {
                seedGeneralDocuments();
            }
        }
    }

    private void removeLegacyArcuSurPlaceholders() {
        repository.findByTemplateType("ARCU-SUR").stream()
                .filter(doc -> doc.getSourceUrl() != null
                        && doc.getSourceUrl().startsWith(LEGACY_ARCU_PLACEHOLDER))
                .forEach(repository::delete);
    }

    private int loadArcuSurCorpus() throws IOException {
        Resource[] files = resources.getResources(ARCU_SUR_CORPUS_PATTERN);
        int upserted = 0;
        for (Resource file : files) {
            String filename = file.getFilename();
            if (filename == null || !filename.endsWith(".md") || filename.equalsIgnoreCase("INDEX.md")) {
                continue;
            }
            String markdown = new String(file.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            var parsed = parser.parse(markdown);
            if (parsed.isEmpty()) {
                log.warn("Skipping unparseable normative markdown {}", filename);
                continue;
            }
            upsertCorpusDocument(filename, parsed.get());
            upserted++;
        }
        return upserted;
    }

    private void upsertCorpusDocument(String filename, NormativeMarkdownDocumentParser.Document parsed) {
        String slug = filename.endsWith(".md") ? filename.substring(0, filename.length() - 3) : filename;
        String sourceUrl = ARCU_SUR_SOURCE_PREFIX + slug;
        NormativeDocumentJpaEntity entity = repository.findBySourceUrl(sourceUrl)
                .orElseGet(() -> NormativeDocumentJpaEntity.builder()
                        .id(UUID.randomUUID())
                        .createdAt(LocalDateTime.now())
                        .build());
        entity.setTitle(parsed.title());
        entity.setTemplateType(parsed.templateType());
        entity.setPhaseName(parsed.phaseName());
        entity.setSubphaseName(parsed.subphaseName());
        entity.setSourceUrl(sourceUrl);
        entity.setBodyText(parsed.bodyText());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        repository.save(entity);
    }

    private void seedCeubDocuments() {
        save(
                "Diagnóstico institucional — CEUB",
                "CEUB",
                "Autoevaluación",
                "Diagnóstico institucional",
                "https://duea.umss.edu.bo/normativa/ceub/diagnostico-institucional",
                """
                El diagnóstico institucional CEUB es la primera subfase de autoevaluación. La universidad \
                debe caracterizar su realidad académica, administrativa y de gestión con evidencia verificable. \
                Incluye análisis de pertinencia, calidad académica, investigación, vinculación con la sociedad \
                y gestión institucional. El equipo DUEA consolida hallazgos que alimentan la matriz de evidencias \
                y prepara el informe de autoevaluación ante el comité externo.
                """);

        save(
                "Matriz de evidencias — CEUB",
                "CEUB",
                "Autoevaluación",
                "Matriz de evidencias",
                "https://duea.umss.edu.bo/normativa/ceub/matriz-evidencias",
                """
                La matriz de evidencias CEUB relaciona cada criterio de evaluación con documentos, \
                indicadores y responsables. Cada evidencia debe tener descripción, archivo respaldatorio, \
                hash de integridad y trazabilidad al criterio. SIGESA registra el estado SUBIDO cuando \
                la carrera carga documentación pendiente de control por DUEA o coordinación de carrera.
                """);

        save(
                "Informe preliminar — CEUB",
                "CEUB",
                "Evaluación externa",
                "Informe preliminar",
                "https://duea.umss.edu.bo/normativa/ceub/informe-preliminar",
                """
                Tras la visita del comité evaluador externo, se emite un informe preliminar con \
                observaciones por criterio. La institución dispone de un plazo para responder y \
                complementar evidencias. Las subfases del proceso deben reflejar este hito con \
                enlaces normativos HTTPS en SIGESA.
                """);

        save(
                "Informe final — CEUB",
                "CEUB",
                "Evaluación externa",
                "Informe final",
                "https://duea.umss.edu.bo/normativa/ceub/informe-final",
                """
                El informe final CEUB consolida la decisión de acreditación y recomendaciones. \
                Debe archivarse junto con las actas del comité y el plan de seguimiento. \
                En SIGESA, la plantilla CEUB 2026 organiza las fases de autoevaluación y evaluación externa \
                para trazabilidad operativa del proceso activo por carrera.
                """);
    }

    private void seedGeneralDocuments() {
        save(
                "Acreditación universitaria UMSS — FAQ SIGESA",
                "GENERAL",
                null,
                null,
                "https://duea.umss.edu.bo/normativa/sigesa/faq",
                """
                SIGESA es el Sistema de Gestión de Acreditación de la UMSS. Gestiona procesos activos \
                por carrera, plantillas CEUB y ARCU-SUR, evidencias por indicador y roles JD, TD, CC y EE. \
                El asistente virtual puede consultar datos operativos vía tools y documentación normativa \
                indexada (RAG) sobre acreditación universitaria, subfases y criterios DUEA.
                """);

        save(
                "Enlaces normativos en subfases",
                "GENERAL",
                null,
                null,
                "https://duea.umss.edu.bo/normativa/sigesa/reference-url",
                """
                Cada subfase del proceso debe incluir un referenceUrl HTTPS válido que apunte a la \
                normativa DUEA o documentación de apoyo. Al crear o editar subfases desde el copiloto \
                de fases, el sistema exige este enlace. list_process_structure expone referenceUrl \
                y descripción por subfase del proceso activo.
                """);
    }

    private void save(String title,
                      String templateType,
                      String phaseName,
                      String subphaseName,
                      String sourceUrl,
                      String bodyText) {
        repository.save(NormativeDocumentJpaEntity.builder()
                .id(UUID.randomUUID())
                .title(title)
                .templateType(templateType)
                .phaseName(phaseName)
                .subphaseName(subphaseName)
                .sourceUrl(sourceUrl)
                .bodyText(bodyText.trim())
                .createdAt(LocalDateTime.now())
                .build());
    }
}
