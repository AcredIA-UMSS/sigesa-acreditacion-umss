package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.UserProgramAssignmentEntity;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.DuplicateActiveAssignmentException;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class UserProgramAssignmentJpaAdapter implements UserProgramAssignmentRepositoryPort {

    private final UserProgramAssignmentJpaRepository jpaRepository;

    public UserProgramAssignmentJpaAdapter(UserProgramAssignmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserProgramAssignment save(UserProgramAssignment assignment) {
        if (assignment.getRevokedAt() == null
                && jpaRepository.existsByUserIdAndProgramIdAndRevokedAtIsNull(
                        assignment.getUserId(), assignment.getProgramId())) {
            throw new DuplicateActiveAssignmentException(assignment.getUserId(), assignment.getProgramId());
        }
        UserProgramAssignmentEntity entity = toEntity(assignment);
        UserProgramAssignmentEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<UserProgramAssignment> findActiveByUserId(UUID userId) {
        return jpaRepository.findByUserIdAndRevokedAtIsNull(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void revokeAllActiveByUserId(UUID userId) {
        jpaRepository.revokeAllActiveByUserId(userId, LocalDateTime.now());
    }

    private UserProgramAssignmentEntity toEntity(UserProgramAssignment assignment) {
        UserProgramAssignmentEntity entity = new UserProgramAssignmentEntity();
        entity.setId(assignment.getId());
        entity.setUserId(assignment.getUserId());
        entity.setProgramId(assignment.getProgramId());
        entity.setAssignedAt(assignment.getAssignedAt());
        entity.setRevokedAt(assignment.getRevokedAt());
        return entity;
    }

    private UserProgramAssignment toDomain(UserProgramAssignmentEntity entity) {
        return new UserProgramAssignment(
                entity.getId(),
                entity.getUserId(),
                entity.getProgramId(),
                entity.getAssignedAt(),
                entity.getRevokedAt()
        );
    }
}
