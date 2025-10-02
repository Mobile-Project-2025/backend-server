package com.mobile.server.domain.auth.jwt;

import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JWTService {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    //AccessToken 발급
    public String generateAccessToken(User user) {
        return jwtUtil.createAccessToken(user.getId(), user.getRole().name());
    }

    //토큰 유효성 검사
    public boolean validateToken(String token) {
        return !jwtUtil.isExpired(token);
    }

    //UserId 추출
    public Long getUserIdFromToken(String token) {
        return jwtUtil.parseUserId(token);
    }

    //Role 추출
    public String getRoleFromToken(String token) {
        return jwtUtil.parseRole(token);
    }

    //Token -> User 조회
    public User findUserFromToken(String token) {
        Long userId = jwtUtil.parseUserId(token);
        return userRepository.findById(userId).orElseThrow();
    }
}
