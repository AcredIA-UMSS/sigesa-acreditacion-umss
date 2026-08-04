package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataAccreditationProcessRepository extends JpaRepository<AccreditationProcessJpaEntity, UUID> {
    boolean existsByCareerIdAndStatus(UUID careerId, String status);

    long countByCareerId(UUID careerId);
}