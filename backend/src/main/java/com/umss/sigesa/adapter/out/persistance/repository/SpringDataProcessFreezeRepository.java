package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.ProcessFreezeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataProcessFreezeRepository extends JpaRepository<ProcessFreezeEntity, UUID> {

    Optional<ProcessFreezeEntity> findByProcessId(UUID processId);
}
