package com.mobile.server.domain.auth.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobile.server.domain.auth.dto.SignInReq;
import com.mobile.server.domain.auth.dto.SignUpReq;
import com.mobile.server.domain.auth.entity.RoleType;
import com.mobile.server.domain.auth.entity.User;
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
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User existingUser;

    @BeforeEach
    void setup() {
        // 기존 사용자 생성
        existingUser = userRepository.save(User.builder()
                .studentId("20251110")
                .nickname("기존유저")
                .password(passwordEncoder.encode("password123"))
                .role(RoleType.STUDENT)
                .cumulativePoint(0L)
                .build());
    }

    @Test
    @DisplayName("성공: 회원가입에 성공한다")
    void register_success() throws Exception {
        // given
        SignUpReq signUpReq = new SignUpReq("새유저", "20251111", "password123");

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpReq))
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("실패: 회원가입 시 학번이 중복되면 409 CONFLICT 반환")
    void register_fail_duplicateStudentId() throws Exception {
        // given
        SignUpReq signUpReq = new SignUpReq("새유저", "20251110", "password123");

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpReq))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("이미 사용 중인 학번입니다."));
    }

    @Test
    @DisplayName("실패: 회원가입 시 닉네임이 중복되면 409 CONFLICT 반환")
    void register_fail_duplicateNickname() throws Exception {
        // given
        SignUpReq signUpReq = new SignUpReq("기존유저", "20251111", "password123");

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpReq))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("이미 사용 중인 닉네임입니다."));
    }

    @Test
    @DisplayName("실패: 회원가입 시 필수 필드가 누락되면 400 BAD REQUEST 반환")
    void register_fail_missingFields() throws Exception {
        // given
        SignUpReq signUpReq = new SignUpReq(null, "20251111", "password123");

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpReq))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공: 학번 중복 체크 - 사용 가능한 학번")
    void checkStudentId_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/auth/check-student-id")
                        .param("studentId", "20251111")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("실패: 학번 중복 체크 - 이미 사용 중인 학번")
    void checkStudentId_fail_duplicate() throws Exception {
        // when & then
        mockMvc.perform(get("/api/auth/check-student-id")
                        .param("studentId", "20251110")
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("이미 사용 중인 학번입니다."));
    }

    @Test
    @DisplayName("성공: 닉네임 중복 체크 - 사용 가능한 닉네임")
    void checkNickname_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/auth/check-nickname")
                        .param("nickname", "새유저")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("실패: 닉네임 중복 체크 - 이미 사용 중인 닉네임")
    void checkNickname_fail_duplicate() throws Exception {
        // when & then
        mockMvc.perform(get("/api/auth/check-nickname")
                        .param("nickname", "기존유저")
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("이미 사용 중인 닉네임입니다."));
    }

    @Test
    @DisplayName("성공: 로그인에 성공한다")
    void login_success() throws Exception {
        // given
        SignInReq signInReq = new SignInReq();
        signInReq.setStudentId("20251110");
        signInReq.setPassword("password123");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInReq))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.nickname").value("기존유저"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("실패: 로그인 시 존재하지 않는 학번으로 시도하면 401 UNAUTHORIZED 반환")
    void login_fail_userNotFound() throws Exception {
        // given
        SignInReq signInReq = new SignInReq();
        signInReq.setStudentId("99999999");
        signInReq.setPassword("password123");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInReq))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("아이디 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("실패: 로그인 시 비밀번호가 틀리면 401 UNAUTHORIZED 반환")
    void login_fail_wrongPassword() throws Exception {
        // given
        SignInReq signInReq = new SignInReq();
        signInReq.setStudentId("20251110");
        signInReq.setPassword("wrongpassword");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInReq))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("아이디 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("성공: 로그아웃에 성공한다")
    void logout_success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}

