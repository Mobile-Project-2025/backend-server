package com.mobile.server.domain.common;

import com.mobile.server.domain.auth.entity.RoleType;
import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class DataInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminId;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (userRepository.findByStudentId(adminId).isEmpty()) {
            User admin = User.builder()
                    .nickname("admin")
                    .studentId(adminId)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(RoleType.ADMIN)
                    .cumulativePoint(99999L)
                    .build();
            userRepository.save(admin);

        }
    }
}
