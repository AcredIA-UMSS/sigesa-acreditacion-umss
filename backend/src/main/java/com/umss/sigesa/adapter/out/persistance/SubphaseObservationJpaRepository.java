package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.SubphaseObservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubphaseObservationJpaRepository extends JpaRepository<SubphaseObservationEntity, UUID> {

    List<SubphaseObservationEntity> findBySubphaseIdOrderByCreatedAtDesc(UUID subphaseId);

    Optional<SubphaseObservationEntity> findFirstBySubphaseIdAndStatusOrderByCreatedAtDesc(
            UUID subphaseId, String status);
}
