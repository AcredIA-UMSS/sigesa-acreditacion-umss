package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.SubphaseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataSubphaseRepository extends JpaRepository<SubphaseJpaEntity, UUID> {

    List<SubphaseJpaEntity> findByPhaseIdOrderByOrderAsc(UUID phaseId);

    Optional<SubphaseJpaEntity> findByIdAndPhaseId(UUID id, UUID phaseId);
}
