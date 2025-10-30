package com.mobile.server.domain.mission.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobile.server.config.FakeS3Uploader;
import com.mobile.server.domain.auth.entity.RoleType;
import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.auth.jwt.CustomUserDetails;
import com.mobile.server.domain.auth.repository.UserRepository;
import com.mobile.server.domain.file.respository.FileRepository;
import com.mobile.server.domain.mission.constant.MissionCategory;
import com.mobile.server.domain.regularMission.RegularMissionRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class MissionManagementControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegularMissionRepository regularMissionRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FakeS3Uploader s3Uploader;

    private User admin;
    private User user1;

    @BeforeEach
    void setup() {
        // 관리자 계정 생성
        admin = userRepository.save(User.builder()
                .studentId("12345678")
                .role(RoleType.ADMIN)
                .nickname("admin")
                .password("test")
                .cumulativePoint(0L)
                .build());

        // 일반 사용자 생성
        user1 = userRepository.save(User.builder()
                .studentId("22332266")
                .role(RoleType.STUDENT)
                .nickname("user")
                .password("password")
                .cumulativePoint(0L)
                .build());

        s3Uploader.clearStorage();

    }

    @Test
    @DisplayName("성공: 관리자 계정이 상시 미션 생성에 성공한다.")
    void createRegularMission_success() throws Exception {
        // given

        MockMultipartFile image = new MockMultipartFile(
                "missionImage", "image.png",
                "image/png", "fake image".getBytes()
        );
        // when & then
        mockMvc.perform(multipart("/api/admin/missions/regular")
                        .file(image)
                        .param("title", "대중교통 이용 챌린지")
                        .param("point", "10")
                        .param("content", "지하철 이용 후 인증샷 업로드")
                        .param("category", MissionCategory.PUBLIC_TRANSPORTATION.name())
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isCreated());

        Assertions.assertThat(regularMissionRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("성공: 관리자 계정이 상시 미션 생성에 성공한다.  파일이 없는 경우")
    void createRegularMission_success_notFile() throws Exception {
        // when & then
        mockMvc.perform(multipart("/api/admin/missions/regular")
                        .param("title", "대중교통 이용 챌린지")
                        .param("point", "10")
                        .param("content", "지하철 이용 후 인증샷 업로드")
                        .param("category", MissionCategory.PUBLIC_TRANSPORTATION.name())
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isCreated());

        Assertions.assertThat(regularMissionRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("실패: 관리자 계정이 상시 미션 생성에 실패한다.유효성 검증에 실패한 경우")
    void createRegularMission_success_isNotValid() throws Exception {
        // when & then
        mockMvc.perform(multipart("/api/admin/missions/regular")
                        .param("title", "대중교통 이용 챌린지")
                        .param("point", "0")
                        .param("content", "지하철 이용 후 인증샷 업로드")
                        .param("category", MissionCategory.PUBLIC_TRANSPORTATION.name())
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("실패: 관리자 상시 미션 생성 - 파라미터 값이 비어있는 경우")
    void createRegularMission_fail_notTitle() throws Exception {
        // when & then
        mockMvc.perform(multipart("/api/admin/missions/regular")
                        .param("point", "10")
                        .param("content", "지하철 이용 후 인증샷 업로드")
                        .param("category", MissionCategory.PUBLIC_TRANSPORTATION.name())
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("실패: 사용자가 상시미션을 생성하는 경우 권한 없음.")
    void createRegularMission_fail_isForbidden() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile(
                "missionImage", "image.png",
                "image/png", "fake image".getBytes()
        );
        // when & then
        mockMvc.perform(multipart("/api/admin/missions/regular")
                        .file(image)
                        .param("title", "대중교통 이용 챌린지")
                        .param("point", "10")
                        .param("content", "지하철 이용 후 인증샷 업로드")
                        .param("category", MissionCategory.PUBLIC_TRANSPORTATION.name())
                        .with(user(new CustomUserDetails(user1)))
                        .with(csrf()))
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("실패: 잘못된 카테고리 입력 시 INVALID_CATEGORY 예외 발생")
    void createRegularMission_fail_invalidCategory() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile(
                "missionImage", "image.png",
                "image/png", "fake image".getBytes()
        );
        // when & then
        mockMvc.perform(multipart("/api/admin/missions/regular")
                        .file(image)
                        .param("title", "대중교통 이용 챌린지")
                        .param("point", "10")
                        .param("content", "지하철 이용 후 인증샷 업로드")
                        .param("category", "test")
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }


}