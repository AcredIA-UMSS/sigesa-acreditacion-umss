package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AuditLogEntity;
import com.umss.sigesa.domain.model.AuditLogQuery;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

final class AuditLogSpecifications {

    private AuditLogSpecifications() {
    }

    static Specification<AuditLogEntity> matching(AuditLogQuery query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.actorId() != null) {
                predicates.add(cb.equal(root.get("actorId"), query.actorId()));
            }
            if (query.actorEmail() != null && !query.actorEmail().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("actorEmail")),
                        query.actorEmail().trim().toLowerCase()));
            }
            if (query.actions() != null && !query.actions().isEmpty()) {
                predicates.add(root.get("action").in(query.actions()));
            }
            if (query.resourceType() != null) {
                predicates.add(cb.equal(root.get("resourceType"), query.resourceType()));
            }
            if (query.resourceId() != null) {
                predicates.add(cb.equal(root.get("resourceId"), query.resourceId()));
            }
            if (query.result() != null) {
                predicates.add(cb.equal(root.get("result"), query.result()));
            }
            if (query.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), query.from()));
            }
            if (query.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("occurredAt"), query.to()));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
