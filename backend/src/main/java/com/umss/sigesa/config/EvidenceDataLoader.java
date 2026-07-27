package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.AppUserJpaRepository;
import com.umss.sigesa.adapter.out.persistance.IndicatorJpaRepository;
import com.umss.sigesa.adapter.out.persistance.UserProgramAssignmentJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AppUserEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEntity;
import com.umss.sigesa.adapter.out.persistance.entity.UserProgramAssignmentEntity;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserStatus;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Order(2)
public class EvidenceDataLoader implements ApplicationRunner {

    public static final String SEED_CC_EMAIL = "cc@umss.edu.bo";
    public static final UUID SEED_PROGRAM_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    public static final UUID SEED_CRITERION_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    public static final UUID SEED_INDICATOR_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
    public static final UUID SEED_PHASE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");

    private final AppUserJpaRepository userRepository;
    private final UserProgramAssignmentJpaRepository assignmentRepository;
    private final IndicatorJpaRepository indicatorRepository;
    private final PasswordEncoder passwordEncoder;

    public EvidenceDataLoader(AppUserJpaRepository userRepository,
                              UserProgramAssignmentJpaRepository assignmentRepository,
                              IndicatorJpaRepository indicatorRepository,
                              PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.indicatorRepository = indicatorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedCoordinatorUser();
        seedIndicator();
    }

    private void seedCoordinatorUser() {
        if (userRepository.findByEmail(SEED_CC_EMAIL).isPresent()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        UUID userId = UUID.randomUUID();

        AppUserEntity cc = new AppUserEntity();
        cc.setId(userId);
        cc.setEmail(SEED_CC_EMAIL);
        cc.setPasswordHash(passwordEncoder.encode(AuthDataLoader.SEED_PASSWORD));
        cc.setRole(Role.CC);
        cc.setStatus(UserStatus.ACTIVE);
        cc.setCreatedAt(now);
        cc.setUpdatedAt(now);
        userRepository.save(cc);

        UserProgramAssignmentEntity assignment = new UserProgramAssignmentEntity();
        assignment.setId(UUID.randomUUID());
        assignment.setUserId(userId);
        assignment.setProgramId(SEED_PROGRAM_ID);
        assignment.setAssignedAt(now);
        assignment.setRevokedAt(null);
        assignmentRepository.save(assignment);
    }

    private void seedIndicator() {
        if (indicatorRepository.existsById(SEED_INDICATOR_ID)) {
            return;
        }
        IndicatorEntity indicator = new IndicatorEntity();
        indicator.setId(SEED_INDICATOR_ID);
        indicator.setProgramId(SEED_PROGRAM_ID);
        indicator.setCriterionId(SEED_CRITERION_ID);
        indicator.setPhaseId(SEED_PHASE_ID);
        indicatorRepository.save(indicator);
    }
}
