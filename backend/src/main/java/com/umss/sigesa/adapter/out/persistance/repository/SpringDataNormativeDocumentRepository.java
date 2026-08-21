package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.NormativeDocumentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataNormativeDocumentRepository extends JpaRepository<NormativeDocumentJpaEntity, UUID> {

    long countByTemplateType(String templateType);

    @Query("""
            SELECT nd FROM NormativeDocumentJpaEntity nd
            WHERE (:templateType IS NULL OR nd.templateType = :templateType)
              AND (LOWER(nd.title) LIKE LOWER(CONCAT('%', :term, '%'))
                OR LOWER(nd.bodyText) LIKE LOWER(CONCAT('%', :term, '%')))
            ORDER BY nd.title ASC
            """)
    List<NormativeDocumentJpaEntity> searchByLike(
            @Param("term") String term,
            @Param("templateType") String templateType,
            org.springframework.data.domain.Pageable pageable);
}
