package com.mobile.server.domain.auth.service;

import com.mobile.server.domain.auth.dto.ProfileResponseDto;
import com.mobile.server.domain.auth.dto.SignUpReq;
import com.mobile.server.domain.auth.entity.RoleType;
import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.auth.repository.UserRepository;
import com.mobile.server.util.exception.BusinessErrorCode;
import com.mobile.server.util.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //회원가입
    @Transactional
    public void register(SignUpReq signUpReq) {
        if (userRepository.existsByStudentId(signUpReq.getStudentId())) {
            throw new BusinessException(BusinessErrorCode.DUPLICATE_STUDENT_ID);
        }

        if (userRepository.existsByNickname(signUpReq.getNickname())) {
            throw new BusinessException(BusinessErrorCode.DUPLICATE_NICKNAME);
        }

        User user = User.builder()
                .studentId(signUpReq.getStudentId())
                .password(passwordEncoder.encode(signUpReq.getPassword()))
                .nickname(signUpReq.getNickname())
                .role(RoleType.STUDENT)
                .cumulativePoint(0L)
                .build();

        userRepository.save(user);
    }

    //학번 중복 체크
    @Transactional(readOnly = true)
    public void checkStudentIdDuplication(String studentId) {
        if (userRepository.existsByStudentId(studentId)) {
            throw new BusinessException(BusinessErrorCode.DUPLICATE_STUDENT_ID);
        }
    }

    //닉네임 중복 체크
    @Transactional(readOnly = true)
    public void checkNicknameDuplication(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(BusinessErrorCode.DUPLICATE_NICKNAME);
        }
    }

    //로그인 사용자 검증
    @Transactional(readOnly = true)
    public User verify(String studentId, String rawPassword) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BusinessException(BusinessErrorCode.INVALID_CREDENTIALS);
        }
        return user;
    }

    //내 프로필 조회
    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.USER_NOT_FOUND));
        return new ProfileResponseDto(user.getNickname(), user.getCumulativePoint());
    }
}
