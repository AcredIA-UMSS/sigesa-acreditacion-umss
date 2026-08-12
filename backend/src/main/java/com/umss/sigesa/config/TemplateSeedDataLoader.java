package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.entity.TemplateJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplatePhaseJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateSubphaseJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateRepository;
import jakarta.persistence.EntityManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Plantillas de demostración CEUB / ARCU-SUR con taxonomía Fase → Subfase.
 */
@Component
@Profile("!prod")
@Order(90)
public class TemplateSeedDataLoader implements ApplicationRunner {

    private final SpringDataTemplateRepository templateRepository;
    private final EntityManager entityManager;

    public TemplateSeedDataLoader(SpringDataTemplateRepository templateRepository,
                                  EntityManager entityManager) {
        this.templateRepository = templateRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedTemplate(
                DevSeedData.TEMPLATE_CEUB_2026,
                "CEUB 2026",
                "CEUB",
                new String[][]{
                        {"Autoevaluación", "Diagnóstico institucional", "Matriz de evidencias"},
                        {"Evaluación externa", "Informe preliminar", "Informe final"}
                }
        );

        seedTemplate(
                DevSeedData.TEMPLATE_ARCUSUR_2026,
                "ARCU-SUR 2026",
                "ARCU-SUR",
                new String[][]{
                        {"Planificación", "Cronograma", "Designación de responsables"},
                        {"Ejecución", "Recolección documental", "Validación de criterios"}
                }
        );
    }

    private void seedTemplate(UUID id, String name, String type, String[][] phaseDefinitions) {
        if (templateRepository.findById(id).isPresent()) {
            return;
        }

        TemplateJpaEntity template = TemplateJpaEntity.builder()
                .id(id)
                .name(name)
                .description("Plantilla normativa de demostración " + type)
                .type(type)
                .status("PUBLISHED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .phases(new ArrayList<>())
                .build();

        for (int phaseIndex = 0; phaseIndex < phaseDefinitions.length; phaseIndex++) {
            String[] phaseDefinition = phaseDefinitions[phaseIndex];
            TemplatePhaseJpaEntity phase = TemplatePhaseJpaEntity.builder()
                    .name(phaseDefinition[0])
                    .order(phaseIndex + 1)
                    .description("Fase " + (phaseIndex + 1))
                    .template(template)
                    .subphases(new ArrayList<>())
                    .build();

            for (int subphaseIndex = 1; subphaseIndex < phaseDefinition.length; subphaseIndex++) {
                String subphaseName = phaseDefinition[subphaseIndex];
                TemplateSubphaseJpaEntity subphase = TemplateSubphaseJpaEntity.builder()
                        .name(subphaseName)
                        .order(subphaseIndex)
                        .referenceUrl("https://duea.umss.edu.bo/normativa/"
                                + slug(type) + "/" + slug(subphaseName))
                        .description("Recurso normativo: " + subphaseName)
                        .templatePhase(phase)
                        .build();
                phase.getSubphases().add(subphase);
            }

            template.getPhases().add(phase);
        }

        entityManager.persist(template);
    }

    private static String slug(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
