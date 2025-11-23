package com.mobile.server.domain.auth.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobile.server.domain.auth.dto.UpdatePointReq;
import com.mobile.server.domain.auth.entity.RoleType;
import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.auth.jwt.CustomUserDetails;
import com.mobile.server.domain.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setup() {
        // 테스트 사용자 생성
        testUser = userRepository.save(User.builder()
                .studentId("20251110")
                .nickname("테스트유저")
                .password(passwordEncoder.encode("password123"))
                .role(RoleType.STUDENT)
                .cumulativePoint(1000L)
                .build());

        userDetails = new CustomUserDetails(testUser);
    }

    @Test
    @DisplayName("성공: 내 프로필 조회에 성공한다")
    void getMyProfile_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users/me")
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("테스트유저"))
                .andExpect(jsonPath("$.cumulativePoint").value(1000));
    }

    @Test
    @DisplayName("실패: 인증되지 않은 사용자가 프로필 조회 시 403 FORBIDDEN 반환")
    void getMyProfile_fail_unauthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users/me")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("성공: 사용자 포인트 수정에 성공한다")
    void updateUserPoint_success() throws Exception {
        // given
        UpdatePointReq updatePointReq = new UpdatePointReq(2000L);

        // when & then
        mockMvc.perform(patch("/api/users/{userId}/points", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePointReq))
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isOk());

        // 포인트가 실제로 업데이트되었는지 확인
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.getCumulativePoint().equals(2000L);
    }

    @Test
    @DisplayName("실패: 포인트 수정 시 음수 값을 입력하면 400 BAD REQUEST 반환")
    void updateUserPoint_fail_negativePoint() throws Exception {
        // given
        UpdatePointReq updatePointReq = new UpdatePointReq(-100L);

        // when & then
        mockMvc.perform(patch("/api/users/{userId}/points", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePointReq))
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패: 포인트 수정 시 포인트 값이 null이면 400 BAD REQUEST 반환")
    void updateUserPoint_fail_nullPoint() throws Exception {
        // given
        UpdatePointReq updatePointReq = new UpdatePointReq(null);

        // when & then
        mockMvc.perform(patch("/api/users/{userId}/points", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePointReq))
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 사용자의 포인트 수정 시 404 NOT FOUND 반환")
    void updateUserPoint_fail_userNotFound() throws Exception {
        // given
        UpdatePointReq updatePointReq = new UpdatePointReq(2000L);
        Long nonExistentUserId = 99999L;

        // when & then
        mockMvc.perform(patch("/api/users/{userId}/points", nonExistentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePointReq))
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("존재하지 않는 사용자입니다."));
    }
}

