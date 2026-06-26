package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.AppUserJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AppUserEntity;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserStatus;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AuthDataLoader implements ApplicationRunner {

    public static final String SEED_JD_EMAIL = "jd@umss.edu.bo";
    public static final String SEED_PASSWORD = "ChangeMe123!";

    private final AppUserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthDataLoader(AppUserJpaRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmail(SEED_JD_EMAIL).isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            AppUserEntity jd = new AppUserEntity();
            jd.setId(UUID.randomUUID());
            jd.setEmail(SEED_JD_EMAIL);
            jd.setPasswordHash(passwordEncoder.encode(SEED_PASSWORD));
            jd.setRole(Role.JD);
            jd.setStatus(UserStatus.ACTIVE);
            jd.setCreatedAt(now);
            jd.setUpdatedAt(now);
            userRepository.save(jd);
        }
    }
}
