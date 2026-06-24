package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AppUserEntity;
import com.umss.sigesa.adapter.out.persistance.entity.UserProgramAssignmentEntity;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
class UserProgramAssignmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserProgramAssignmentJpaRepository assignmentRepository;

    @Autowired
    private AppUserJpaRepository userRepository;

    @Test
    void saveAssignment_persistsForeignKeyToUser() {
        AppUserEntity user = persistUser("cc@umss.edu.bo");
        UUID programId = UUID.randomUUID();

        UserProgramAssignmentEntity assignment = new UserProgramAssignmentEntity();
        assignment.setId(UUID.randomUUID());
        assignment.setUserId(user.getId());
        assignment.setProgramId(programId);
        assignment.setAssignedAt(LocalDateTime.now());

        assignmentRepository.save(assignment);
        entityManager.flush();
        entityManager.clear();

        List<UserProgramAssignmentEntity> active = assignmentRepository.findByUserIdAndRevokedAtIsNull(user.getId());
        assertEquals(1, active.size());
        assertEquals(programId, active.get(0).getProgramId());
        assertNull(active.get(0).getRevokedAt());
    }

    @Test
    void revokeAllActiveByUserId_setsRevokedAtWithoutDeletingHistory() {
        AppUserEntity user = persistUser("td@umss.edu.bo");
        UserProgramAssignmentEntity assignment = new UserProgramAssignmentEntity();
        assignment.setId(UUID.randomUUID());
        assignment.setUserId(user.getId());
        assignment.setProgramId(UUID.randomUUID());
        assignment.setAssignedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        assignmentRepository.revokeAllActiveByUserId(user.getId(), LocalDateTime.now());
        entityManager.flush();
        entityManager.clear();

        assertEquals(0, assignmentRepository.findByUserIdAndRevokedAtIsNull(user.getId()).size());
        UserProgramAssignmentEntity historical = assignmentRepository.findById(assignment.getId()).orElseThrow();
        assertNotNull(historical.getRevokedAt());
    }

    private AppUserEntity persistUser(String email) {
        AppUserEntity user = new AppUserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(Role.TD);
        user.setStatus(UserStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return userRepository.save(user);
    }
}
