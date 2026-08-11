package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.AppUserJpaRepository;
import com.umss.sigesa.adapter.out.persistance.EvaluationCriterionJpaRepository;
import com.umss.sigesa.adapter.out.persistance.EvaluationDimensionJpaRepository;
import com.umss.sigesa.adapter.out.persistance.IndicatorJpaRepository;
import com.umss.sigesa.adapter.out.persistance.UserProgramAssignmentJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AppUserEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvaluationCriterionEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvaluationDimensionEntity;
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
    public static final UUID SEED_PROGRAM_ID = DevSeedData.PROGRAM_INF_SIS;
    public static final UUID SEED_DIMENSION_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    public static final UUID SEED_CRITERION_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    public static final UUID SEED_INDICATOR_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
    public static final UUID SEED_PHASE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");

    private final AppUserJpaRepository userRepository;
    private final UserProgramAssignmentJpaRepository assignmentRepository;
    private final IndicatorJpaRepository indicatorRepository;
    private final EvaluationDimensionJpaRepository dimensionRepository;
    private final EvaluationCriterionJpaRepository criterionRepository;
    private final PasswordEncoder passwordEncoder;

    public EvidenceDataLoader(AppUserJpaRepository userRepository,
                              UserProgramAssignmentJpaRepository assignmentRepository,
                              IndicatorJpaRepository indicatorRepository,
                              EvaluationDimensionJpaRepository dimensionRepository,
                              EvaluationCriterionJpaRepository criterionRepository,
                              PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.indicatorRepository = indicatorRepository;
        this.dimensionRepository = dimensionRepository;
        this.criterionRepository = criterionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedCoordinatorUser();
        seedTaxonomy();
        seedIndicator();
    }

    private void seedCoordinatorUser() {
        AppUserEntity cc = userRepository.findByEmail(SEED_CC_EMAIL).orElse(null);
        LocalDateTime now = LocalDateTime.now();

        if (cc == null) {
            cc = new AppUserEntity();
            cc.setId(UUID.randomUUID());
            cc.setEmail(SEED_CC_EMAIL);
            cc.setPasswordHash(passwordEncoder.encode(AuthDataLoader.SEED_CC_PASSWORD));
            cc.setRole(Role.CC);
            cc.setStatus(UserStatus.ACTIVE);
            cc.setCreatedAt(now);
            cc.setUpdatedAt(now);
            cc = userRepository.save(cc);
        }

        boolean hasAssignment = assignmentRepository.existsByUserIdAndProgramIdAndRevokedAtIsNull(cc.getId(), SEED_PROGRAM_ID);
        if (!hasAssignment) {
            UserProgramAssignmentEntity assignment = new UserProgramAssignmentEntity();
            assignment.setId(UUID.randomUUID());
            assignment.setUserId(cc.getId());
            assignment.setProgramId(SEED_PROGRAM_ID);
            assignment.setAssignedAt(now);
            assignment.setRevokedAt(null);
            assignmentRepository.save(assignment);
        }
    }

    private void seedTaxonomy() {
        if (!dimensionRepository.existsById(SEED_DIMENSION_ID)) {
            EvaluationDimensionEntity dim = new EvaluationDimensionEntity();
            dim.setId(SEED_DIMENSION_ID);
            dim.setTemplateId(UUID.randomUUID());
            dim.setCode("DIM-01");
            dim.setName("Infraestructura");
            dimensionRepository.save(dim);
        }
        if (!criterionRepository.existsById(SEED_CRITERION_ID)) {
            EvaluationCriterionEntity crit = new EvaluationCriterionEntity();
            crit.setId(SEED_CRITERION_ID);
            crit.setDimensionId(SEED_DIMENSION_ID);
            crit.setCode("CRT-04");
            crit.setDescription("Infraestructura física y equipamiento de laboratorios");
            criterionRepository.save(crit);
        }
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
