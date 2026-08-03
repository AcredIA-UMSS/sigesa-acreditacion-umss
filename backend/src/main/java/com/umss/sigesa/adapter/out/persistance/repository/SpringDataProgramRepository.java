package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.ProgramJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataProgramRepository extends JpaRepository<ProgramJpaEntity, UUID> {

    @Query("""
            SELECT p FROM ProgramJpaEntity p
            WHERE p.active = true
              AND (
                :query IS NULL OR :query = ''
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY p.name ASC
            """)
    List<ProgramJpaEntity> searchActive(@Param("query") String query);
}
