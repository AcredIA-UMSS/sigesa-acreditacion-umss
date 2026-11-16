package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.entity.PublicationSnapshotJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataPublicationSnapshotRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Snapshots públicos para FSD-UC-016. INF-SIS publicado con certificado (CEUB / DUEA-A 048/2024);
 * Medicina publicada sin certificado; Ingeniería Civil no publicada (BR-10).
 */
@Component
@Profile("!prod")
@Order(90)
public class PublicPortalSeedDataLoader implements ApplicationRunner {

    public static final UUID SNAPSHOT_INF_SIS = UUID.fromString("660e8400-e29b-41d4-a716-446655440100");
    public static final UUID SNAPSHOT_MED = UUID.fromString("660e8400-e29b-41d4-a716-446655440101");
    public static final UUID SNAPSHOT_ING_CIV_DRAFT = UUID.fromString("660e8400-e29b-41d4-a716-446655440102");

    public static final String SLUG_INF_SIS = "inf-sis";
    public static final String SLUG_MED = "med";
    public static final String SLUG_ING_CIV_DRAFT = "ing-civ";

    public static final String DEFAULT_AGENCY = "Comité Ejecutivo de la Universidad Boliviana (CEUB)";
    public static final String DEFAULT_RESOLUTION = "DUEA-A 048/2024";

    private final SpringDataPublicationSnapshotRepository repository;

    public PublicPortalSeedDataLoader(SpringDataPublicationSnapshotRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed(SNAPSHOT_INF_SIS, SLUG_INF_SIS, "Ingeniería de Sistemas", "ACREDITADA",
                LocalDate.of(2025, 1, 1), LocalDate.of(2029, 12, 31), true, true,
                DEFAULT_AGENCY, DEFAULT_RESOLUTION);
        seed(SNAPSHOT_MED, SLUG_MED, "Medicina", "EN_PROCESO",
                LocalDate.of(2026, 3, 1), LocalDate.of(2027, 2, 28), true, false,
                null, null);
        seed(SNAPSHOT_ING_CIV_DRAFT, SLUG_ING_CIV_DRAFT, "Ingeniería Civil", "BORRADOR",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), false, false,
                null, null);
    }

    private void seed(UUID id,
                      String slug,
                      String name,
                      String status,
                      LocalDate from,
                      LocalDate until,
                      boolean published,
                      boolean certificatePublished,
                      String agency,
                      String resolutionNumber) {
        PublicationSnapshotJpaEntity entity = repository.findById(id)
                .orElseGet(() -> PublicationSnapshotJpaEntity.builder().id(id).build());

        entity.setSlug(slug);
        entity.setProgramName(name);
        entity.setAccreditationStatus(status);
        entity.setValidFrom(from);
        entity.setValidUntil(until);
        entity.setPublished(published);
        entity.setCertificatePublished(certificatePublished);
        if (agency != null) {
            entity.setAgency(agency);
        }
        if (resolutionNumber != null) {
            entity.setResolutionNumber(resolutionNumber);
        }
        repository.save(entity);
    }
}
