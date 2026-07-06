package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.AppUserJpaRepository;
import com.umss.sigesa.adapter.out.persistance.UserProgramAssignmentJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AppUserEntity;
import com.umss.sigesa.adapter.out.persistance.entity.UserProgramAssignmentEntity;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserStatus;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cuentas de prueba para desarrollo local (H2). No usar en producción.
 */
@Component
public class AuthDataLoader implements ApplicationRunner {

    public static final String SEED_JD_EMAIL = "jd@umss.edu.bo";
    public static final String SEED_TD_EMAIL = "td@umss.edu.bo";
    public static final String SEED_CC_EMAIL = "cc@umss.edu.bo";

    public static final String SEED_JD_PASSWORD = "JefeDemo2026!";
    public static final String SEED_TD_PASSWORD = "TecnicoDemo2026!";
    public static final String SEED_CC_PASSWORD = "CoordDemo2026!";

    /** Carrera ficticia de prueba para asignación del coordinador seed. */
    public static final UUID SEED_PROGRAM_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

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
        seedUser(SEED_JD_EMAIL, SEED_JD_PASSWORD, Role.JD, null);
        seedUser(SEED_TD_EMAIL, SEED_TD_PASSWORD, Role.TD, null);
        seedUser(SEED_CC_EMAIL, SEED_CC_PASSWORD, Role.CC, SEED_PROGRAM_ID);
    }

    private void seedUser(String email, String password, Role role, UUID programId) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        UUID userId = UUID.randomUUID();

        AppUserEntity user = new AppUserEntity();
        user.setId(userId);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);

        if (programId != null) {
            UserProgramAssignmentEntity assignment = new UserProgramAssignmentEntity();
            assignment.setId(UUID.randomUUID());
            assignment.setUserId(userId);
            assignment.setProgramId(programId);
            assignment.setAssignedAt(now);
            assignmentRepository.save(assignment);
        }
    }
}
