package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Solo alta y lectura. No declara update ni delete: la bitácora es append-only
 * en la aplicación. En PostgreSQL el trigger de V8 refuerza esa restricción.
 */
public interface AuditLogJpaRepository extends Repository<AuditLogEntity, UUID>,
        JpaSpecificationExecutor<AuditLogEntity> {

    AuditLogEntity save(AuditLogEntity entity);

    Optional<AuditLogEntity> findById(UUID id);
}
