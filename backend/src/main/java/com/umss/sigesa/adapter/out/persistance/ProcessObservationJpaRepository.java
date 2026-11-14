package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ProcessObservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcessObservationJpaRepository extends JpaRepository<ProcessObservationJpaEntity, UUID> {

    List<ProcessObservationJpaEntity> findByProcessIdOrderByUpdatedAtDesc(UUID processId);
}
