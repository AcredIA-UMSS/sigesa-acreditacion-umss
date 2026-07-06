package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.TemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TemplateJpaRepository extends JpaRepository<TemplateEntity, UUID> {
}
