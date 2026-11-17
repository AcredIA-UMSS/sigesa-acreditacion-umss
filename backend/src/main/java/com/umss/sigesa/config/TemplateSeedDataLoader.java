package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.entity.TemplateJpaEntity;
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
 * Plantillas de demostración CEUB / ARCU-SUR (metadatos; árbol normativo v2 se carga aparte).
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
        seedTemplate(DevSeedData.TEMPLATE_CEUB_2026, "CEUB 2026", "CEUB");
        seedTemplate(DevSeedData.TEMPLATE_ARCUSUR_2026, "ARCU-SUR 2026", "ARCU-SUR");
    }

    private void seedTemplate(UUID id, String name, String type) {
        if (templateRepository.findById(id).isPresent()) {
            return;
        }

        TemplateJpaEntity template = TemplateJpaEntity.builder()
                .id(id)
                .name(name)
                .description("Plantilla normativa de demostración " + type)
                .type(type)
                .evaluatorModel(type)
                .status("PUBLISHED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .level1Nodes(new ArrayList<>())
                .build();

        entityManager.persist(template);
    }
}
