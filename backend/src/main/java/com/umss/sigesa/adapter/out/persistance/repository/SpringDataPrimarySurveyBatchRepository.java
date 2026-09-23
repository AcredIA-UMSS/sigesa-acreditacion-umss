package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.PrimarySurveyBatchJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataPrimarySurveyBatchRepository extends JpaRepository<PrimarySurveyBatchJpaEntity, UUID> {

    long countByProcessId(UUID processId);
}
