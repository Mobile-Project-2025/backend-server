package com.mobile.domain.auth.service;

import com.mobile.domain.auth.dto.SignUpReq;
import com.mobile.domain.auth.entity.RoleType;
import com.mobile.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(SignUpReq signUpReq) {
        if (userRepository.existsByLoginId(signUpReq.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        User user = User.builder()
                .studentId(signUpReq.getStudentId())
                .password(passwordEncoder.encode(signUpReq.getPassword()))
                .nickname(signUpReq.getNickname())
                .role(RoleType.STUDENT)
                .build();

        userRepository.save(user);
    }
}
