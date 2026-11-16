package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.PublicationSnapshotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataPublicationSnapshotRepository extends JpaRepository<PublicationSnapshotJpaEntity, UUID> {

    List<PublicationSnapshotJpaEntity> findByPublishedTrueOrderByProgramNameAsc();

    Optional<PublicationSnapshotJpaEntity> findBySlug(String slug);
}
