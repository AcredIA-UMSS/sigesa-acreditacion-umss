package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessEntity;
import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.ProcessType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccreditationProcessJpaRepository extends JpaRepository<AccreditationProcessEntity, UUID> {

    boolean existsByCareerIdAndTypeAndPeriodAndStatus(
            UUID careerId, ProcessType type, String period, ProcessStatus status);
}
