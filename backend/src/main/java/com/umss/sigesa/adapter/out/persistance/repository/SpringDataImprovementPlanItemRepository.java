package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.ImprovementPlanItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataImprovementPlanItemRepository extends JpaRepository<ImprovementPlanItemEntity, UUID> {

    List<ImprovementPlanItemEntity> findByProcessIdOrderByCreatedAtAsc(UUID processId);

    Optional<ImprovementPlanItemEntity> findByIdAndProcessId(UUID id, UUID processId);

    boolean existsByProcessIdAndSourceIndicatorId(UUID processId, UUID sourceIndicatorId);
}
