package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorDelegationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataIndicatorDelegationRepository extends JpaRepository<IndicatorDelegationEntity, UUID> {

    List<IndicatorDelegationEntity> findByProcessIdOrderByCreatedAtAsc(UUID processId);

    List<IndicatorDelegationEntity> findByProcessIdAndUserId(UUID processId, UUID userId);

    Optional<IndicatorDelegationEntity> findByIdAndProcessId(UUID id, UUID processId);
}
