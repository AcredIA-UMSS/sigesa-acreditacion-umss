package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.entity.NormativeDocumentJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataNormativeDocumentRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Corpus normativo inicial para RAG del asistente (CEUB, ARCU-SUR y FAQ SIGESA).
 */
@Component
@Profile("!prod")
@Order(95)
public class NormativeDocumentSeedLoader implements ApplicationRunner {

    private final SpringDataNormativeDocumentRepository repository;

    public NormativeDocumentSeedLoader(SpringDataNormativeDocumentRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }

        seedCeubDocuments();
        seedArcuSurDocuments();
        seedGeneralDocuments();
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

    private void seedArcuSurDocuments() {
        save(
                "Cronograma de acreditación — ARCU-SUR",
                "ARCU-SUR",
                "Planificación",
                "Cronograma",
                "https://duea.umss.edu.bo/normativa/arcu-sur/cronograma",
                """
                El cronograma ARCU-SUR define hitos de planificación, recolección documental y validación \
                de criterios regionales. Debe alinearse con el calendario del comité ARCU-SUR y publicarse \
                al equipo de carrera. SIGESA permite vincular subfases con referenceUrl HTTPS hacia \
                la normativa DUEA correspondiente.
                """);

        save(
                "Designación de responsables — ARCU-SUR",
                "ARCU-SUR",
                "Planificación",
                "Designación de responsables",
                "https://duea.umss.edu.bo/normativa/arcu-sur/designacion-responsables",
                """
                Cada subfase ARCU-SUR requiere responsables institucionales (JD, TD, CC) con alcance \
                definido. La designación debe documentarse antes de la recolección documental. \
                En SIGESA, process_responsible_assignment registra al responsable del proceso activo.
                """);

        save(
                "Recolección documental — ARCU-SUR",
                "ARCU-SUR",
                "Ejecución",
                "Recolección documental",
                "https://duea.umss.edu.bo/normativa/arcu-sur/recoleccion-documental",
                """
                La recolección documental ARCU-SUR exige evidencias por indicador, con control de versión \
                y hash SHA-256. Los coordinadores de carrera cargan archivos; DUEA valida completitud \
                antes del control formal. Estados: PENDIENTE, SUBIDO, APROBADO, OBSERVADO.
                """);

        save(
                "Validación de criterios — ARCU-SUR",
                "ARCU-SUR",
                "Ejecución",
                "Validación de criterios",
                "https://duea.umss.edu.bo/normativa/arcu-sur/validacion-criterios",
                """
                La validación de criterios ARCU-SUR verifica que cada indicador cumple los estándares \
                regionales de acreditación universitaria. El checklist de completitud en SIGESA revisa \
                archivo, descripción, criterio, hash y estado actual del indicador.
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
