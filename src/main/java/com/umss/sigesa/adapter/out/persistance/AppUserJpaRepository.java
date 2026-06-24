package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserJpaRepository extends JpaRepository<AppUserEntity, UUID> {

    Optional<AppUserEntity> findByEmail(String email);
}
