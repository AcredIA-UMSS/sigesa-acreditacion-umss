package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.PublicationSnapshotJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataPublicationSnapshotRepository;
import com.umss.sigesa.application.port.out.PublicProgramQueryPort;
import com.umss.sigesa.domain.model.PublicProgramSnapshot;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PublicProgramQueryJpaAdapter implements PublicProgramQueryPort {

    private final SpringDataPublicationSnapshotRepository repository;

    public PublicProgramQueryJpaAdapter(SpringDataPublicationSnapshotRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PublicProgramSnapshot> findPublished() {
        return repository.findByPublishedTrueOrderByProgramNameAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<PublicProgramSnapshot> findBySlug(String slug) {
        return repository.findBySlug(slug).map(this::toDomain);
    }

    private PublicProgramSnapshot toDomain(PublicationSnapshotJpaEntity entity) {
        return new PublicProgramSnapshot(
                entity.getId(),
                entity.getSlug(),
                entity.getProgramName(),
                entity.getAccreditationStatus(),
                entity.getValidFrom(),
                entity.getValidUntil(),
                entity.isPublished(),
                entity.isCertificatePublished()
        );
    }
}
