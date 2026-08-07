package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.AppUserJpaRepository;
import com.umss.sigesa.adapter.out.persistance.UserProgramAssignmentJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AppUserEntity;
import com.umss.sigesa.adapter.out.persistance.entity.UserProgramAssignmentEntity;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserStatus;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cuentas de prueba para desarrollo local (H2). No usar en producción.
 */
@Component
@Profile("!prod")
@Order(100)
public class AuthDataLoader implements ApplicationRunner {

    public static final String SEED_JD_EMAIL = "jd@umss.edu.bo";
    public static final String SEED_TD_EMAIL = "td@umss.edu.bo";
    public static final String SEED_CC_EMAIL = "cc@umss.edu.bo";
    public static final String SEED_CC2_EMAIL = "cc2@umss.edu.bo";
    public static final String SEED_PENDING_EMAIL = "pendiente@umss.edu.bo";
    public static final String SEED_EE_EMAIL = "ee@umss.edu.bo";

    public static final String SEED_JD_PASSWORD = "JefeDemo2026!";
    public static final String SEED_TD_PASSWORD = "TecnicoDemo2026!";
    public static final String SEED_CC_PASSWORD = "CoordDemo2026!";
    public static final String SEED_CC2_PASSWORD = "Coord2Demo2026!";
    public static final String SEED_PENDING_PASSWORD = "PendienteDemo2026!";
    public static final String SEED_EE_PASSWORD = "EvalDemo2026!";

    /** @deprecated Usar {@link DevSeedData#PROGRAM_INF_SIS}. */
    @Deprecated
    public static final UUID SEED_PROGRAM_ID = DevSeedData.PROGRAM_INF_SIS;

    private final AppUserJpaRepository userRepository;
    private final UserProgramAssignmentJpaRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthDataLoader(AppUserJpaRepository userRepository,
                            UserProgramAssignmentJpaRepository assignmentRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedUser(SEED_JD_EMAIL, SEED_JD_PASSWORD, Role.JD, UserStatus.ACTIVE, null);
        seedUser(SEED_TD_EMAIL, SEED_TD_PASSWORD, Role.TD, UserStatus.ACTIVE, null);
        seedUser(SEED_CC_EMAIL, SEED_CC_PASSWORD, Role.CC, UserStatus.ACTIVE, DevSeedData.PROGRAM_INF_SIS);
        seedUser(SEED_CC2_EMAIL, SEED_CC2_PASSWORD, Role.CC, UserStatus.ACTIVE, DevSeedData.PROGRAM_ING_CIVIL);
        seedUser(SEED_PENDING_EMAIL, SEED_PENDING_PASSWORD, Role.CC, UserStatus.INACTIVE, DevSeedData.PROGRAM_MEDICINA);
        seedUser(SEED_EE_EMAIL, SEED_EE_PASSWORD, Role.EE, UserStatus.ACTIVE, DevSeedData.PROGRAM_INF_SIS);
    }

    private void seedUser(String email, String password, Role role, UserStatus status, UUID programId) {
        LocalDateTime now = LocalDateTime.now();
        AppUserEntity user = userRepository.findByEmail(email).orElseGet(() -> {
            AppUserEntity newUser = new AppUserEntity();
            newUser.setId(UUID.randomUUID());
            newUser.setCreatedAt(now);
            return newUser;
        });
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus(status);
        user.setUpdatedAt(now);
        user.setFirstName("Demo");
        user.setLastName(role.name());
        user.setPhoneNumber("71234567");
        userRepository.save(user);

        if (programId != null) {
            boolean hasAssignment = assignmentRepository.existsByUserIdAndProgramIdAndRevokedAtIsNull(user.getId(), programId);
            if (!hasAssignment) {
                UserProgramAssignmentEntity assignment = new UserProgramAssignmentEntity();
                assignment.setId(UUID.randomUUID());
                assignment.setUserId(user.getId());
                assignment.setProgramId(programId);
                assignment.setAssignedAt(now);
                assignmentRepository.save(assignment);
            }
        }
    }
}
