package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ReportExportJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ReportExportJobJpaRepository extends JpaRepository<ReportExportJobEntity, UUID> {
}
