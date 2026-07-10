package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.AccreditationProcessJpaRepository;
import com.umss.sigesa.adapter.out.persistance.TemplateJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateEntity;
import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.ProcessType;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Carga plantillas y procesos de acreditación de demostración en H2 (desarrollo local).
 */
@Component
@Profile("!prod")
@Order(200)
public class DevDataLoader implements ApplicationRunner {

    private final TemplateJpaRepository templateRepository;
    private final AccreditationProcessJpaRepository processRepository;

    public DevDataLoader(TemplateJpaRepository templateRepository,
                         AccreditationProcessJpaRepository processRepository) {
        this.templateRepository = templateRepository;
        this.processRepository = processRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedTemplates();
        seedProcesses();
    }

    private void seedTemplates() {
        seedTemplate(DevSeedData.TEMPLATE_CEUB_2026, true, DevSeedData.TAXONOMY_CEUB_VERSION);
        seedTemplate(DevSeedData.TEMPLATE_ARCUSUR_2026, true, DevSeedData.TAXONOMY_ARCUSUR_VERSION);
        seedTemplate(DevSeedData.TEMPLATE_DRAFT, false, DevSeedData.TAXONOMY_DRAFT_VERSION);
    }

    private void seedTemplate(java.util.UUID id, boolean validated, String taxonomyVersion) {
        TemplateEntity template = templateRepository.findById(id).orElseGet(TemplateEntity::new);
        template.setId(id);
        template.setValidated(validated);
        template.setTaxonomyVersion(taxonomyVersion);
        if (validated && template.getActivePeriod() == null) {
            template.setActivePeriod(DevSeedData.PERIOD_2026_1);
            template.setActivatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        }
        templateRepository.save(template);
    }

    private void seedProcesses() {
        LocalDateTime base = LocalDateTime.of(2026, 1, 15, 10, 0);

        seedProcess(
                DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE,
                DevSeedData.TEMPLATE_CEUB_2026,
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PERIOD_2026_1,
                ProcessType.CEUB,
                ProcessStatus.ACTIVE,
                DevSeedData.TAXONOMY_CEUB_VERSION,
                base
        );

        seedProcess(
                DevSeedData.PROCESS_CEUB_CLOSED,
                DevSeedData.TEMPLATE_CEUB_2026,
                DevSeedData.PROGRAM_CEUB,
                DevSeedData.PERIOD_2025_2,
                ProcessType.CEUB,
                ProcessStatus.CLOSED,
                DevSeedData.TAXONOMY_CEUB_VERSION,
                base.minusMonths(6)
        );

        seedProcess(
                DevSeedData.PROCESS_ARCUSUR_ARCHIVED,
                DevSeedData.TEMPLATE_ARCUSUR_2026,
                DevSeedData.PROGRAM_ARCUSUR,
                DevSeedData.PERIOD_2025_2,
                ProcessType.ARCU_SUR,
                ProcessStatus.ARCHIVED,
                DevSeedData.TAXONOMY_ARCUSUR_VERSION,
                base.minusYears(1)
        );
    }

    private void seedProcess(java.util.UUID id,
                             java.util.UUID templateId,
                             java.util.UUID careerId,
                             String period,
                             ProcessType type,
                             ProcessStatus status,
                             String taxonomySnapshotVersion,
                             LocalDateTime createdAt) {
        if (processRepository.existsById(id)) {
            return;
        }
        AccreditationProcessEntity process = new AccreditationProcessEntity();
        process.setId(id);
        process.setTemplateId(templateId);
        process.setCareerId(careerId);
        process.setPeriod(period);
        process.setType(type);
        process.setStatus(status);
        process.setTaxonomySnapshotVersion(taxonomySnapshotVersion);
        process.setCreatedAt(createdAt);
        processRepository.save(process);
    }
}
