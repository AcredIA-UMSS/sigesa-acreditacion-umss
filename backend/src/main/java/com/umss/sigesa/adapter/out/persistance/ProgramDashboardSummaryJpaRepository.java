package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ProgramDashboardSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProgramDashboardSummaryJpaRepository extends JpaRepository<ProgramDashboardSummaryEntity, UUID> {
}
