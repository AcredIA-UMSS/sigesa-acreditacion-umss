package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IndicatorJpaRepository extends JpaRepository<IndicatorEntity, UUID> {
}
