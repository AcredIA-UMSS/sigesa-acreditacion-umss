package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.EvaluationCriterionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EvaluationCriterionJpaRepository extends JpaRepository<EvaluationCriterionEntity, UUID> {
}
