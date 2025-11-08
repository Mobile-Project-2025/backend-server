package com.mobile.server.domain.mission.controller;

import static com.mobile.server.domain.file.domain.File.ofParticipation;
import static com.mobile.server.domain.mission.e.MissionStatus.CLOSED;
import static com.mobile.server.domain.mission.e.MissionStatus.OPEN;
import static com.mobile.server.domain.mission.e.MissionType.EVENT;
import static com.mobile.server.domain.missionParticipation.domain.MissionParticipation.builder;
import static com.mobile.server.domain.missionParticipation.eum.MissionParticipationStatus.APPROVED;
import static com.mobile.server.domain.missionParticipation.eum.MissionParticipationStatus.PENDING;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobile.server.config.FakeS3Uploader;
import com.mobile.server.domain.auth.entity.RoleType;
import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.auth.jwt.CustomUserDetails;
import com.mobile.server.domain.auth.repository.UserRepository;
import com.mobile.server.domain.file.respository.FileRepository;
import com.mobile.server.domain.mission.constant.MissionCategory;
import com.mobile.server.domain.mission.domain.Mission;
import com.mobile.server.domain.mission.dto.RegularMissionCreationDto;
import com.mobile.server.domain.mission.repository.MissionRepository;
import com.mobile.server.domain.missionParticipation.repository.MissionParticipationRepository;
import com.mobile.server.domain.regularMission.RegularMissionRepository;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private MissionParticipationRepository missionParticipationRepository;

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
    @DisplayName("성공: 관리자 계정이 돌발 미션 생성에 성공한다.")
    void createEventMission_success() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile(
                "missionImage", "image.png",
                "image/png", "fake image".getBytes()
        );
        // when & then
        mockMvc.perform(multipart("/api/admin/missions/event")
                        .file(image)
                        .param("title", "대중교통 이용 챌린지")
                        .param("point", "10")
                        .param("content", "지하철 이용 후 인증샷 업로드")
                        .param("category", MissionCategory.PUBLIC_TRANSPORTATION.name())
                        .param("startDate", LocalDate.now().toString())
                        .param("deadLine", LocalDate.now().toString())
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isCreated());

        Assertions.assertThat(missionRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("성공: 관리자 계정이 돌발 미션 생성에 성공한다. - 파일이 없는 경우")
    void createEventMission_success_notFile() throws Exception {
        // when & then
        mockMvc.perform(multipart("/api/admin/missions/event")
                        .param("title", "대중교통 이용 챌린지")
                        .param("point", "10")
                        .param("content", "지하철 이용 후 인증샷 업로드")
                        .param("category", MissionCategory.PUBLIC_TRANSPORTATION.name())
                        .param("startDate", LocalDate.now().toString())
                        .param("deadLine", LocalDate.now().toString())
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isCreated());

        Assertions.assertThat(missionRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("실패: 사용자가 돌발 미션 생성을 시도한다.")
    void createEventMission_fail_isForbidden() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile(
                "missionImage", "image.png",
                "image/png", "fake image".getBytes()
        );
        // when & then
        mockMvc.perform(multipart("/api/admin/missions/event")
                        .file(image)
                        .param("title", "대중교통 이용 챌린지")
                        .param("point", "10")
                        .param("content", "지하철 이용 후 인증샷 업로드")
                        .param("category", MissionCategory.PUBLIC_TRANSPORTATION.name())
                        .param("startDate", LocalDate.now().toString())
                        .param("deadLine", LocalDate.now().toString())
                        .with(user(new CustomUserDetails(user1)))
                        .with(csrf()))
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("실패: 관리자 계정이 돌발 미션 생성에 성공한다. - 시작날짜 > 종료날짜")
    void createEventMission_fail_inValidDate() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile(
                "missionImage", "image.png",
                "image/png", "fake image".getBytes()
        );
        // when & then
        mockMvc.perform(multipart("/api/admin/missions/event")
                        .file(image)
                        .param("title", "대중교통 이용 챌린지")
                        .param("point", "10")
                        .param("content", "지하철 이용 후 인증샷 업로드")
                        .param("category", MissionCategory.PUBLIC_TRANSPORTATION.name())
                        .param("startDate", LocalDate.now().toString() + 1)
                        .param("deadLine", LocalDate.now().toString())
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

    }


    @Test
    @DisplayName("성공: 관리자 계정이 상시 미션 생성에 성공한다.")
    void createRegularMission_success() throws Exception {
        // given
        RegularMissionCreationDto dto = new RegularMissionCreationDto("대중 교통이용 챌린지", 10L, "지하철 이용 후 인증샷 업로드",
                MissionCategory.PUBLIC_TRANSPORTATION.name());

        // when & then
        mockMvc.perform(post("/api/admin/missions/regular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isCreated());

        Assertions.assertThat(regularMissionRepository.findAll().size()).isEqualTo(1);
    }


    @Test
    @DisplayName("실패: 관리자 계정이 상시 미션 생성에 실패한다.유효성 검증에 실패한 경우")
    void createRegularMission_success_isNotValid() throws Exception {
        // when & then
        RegularMissionCreationDto dto = new RegularMissionCreationDto("대중교통 이용 챌린지", 0L, "지하철 이용 후 인증샷 업로드",
                MissionCategory.PUBLIC_TRANSPORTATION.name());

        // when & then
        mockMvc.perform(post("/api/admin/missions/regular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("실패: 관리자 상시 미션 생성 - 파라미터 값이 비어있는 경우")
    void createRegularMission_fail_notTitle() throws Exception {
        // when & then
        RegularMissionCreationDto dto = new RegularMissionCreationDto(null, 10L, "지하철 이용 후 인증샷 업로드",
                MissionCategory.PUBLIC_TRANSPORTATION.name());

        // when & then
        mockMvc.perform(post("/api/admin/missions/regular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("실패: 사용자가 상시미션을 생성하는 경우 권한 없음.")
    void createRegularMission_fail_isForbidden() throws Exception {
        // given
        RegularMissionCreationDto dto = new RegularMissionCreationDto("대중교통 이용 챌린지", 10L, "지하철 이용 후 인증샷 업로드",
                MissionCategory.PUBLIC_TRANSPORTATION.name());

        // when & then
        mockMvc.perform(post("/api/admin/missions/regular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(user(new CustomUserDetails(user1)))
                        .with(csrf()))
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("실패: 잘못된 카테고리 입력 시 INVALID_CATEGORY 예외 발생")
    void createRegularMission_fail_invalidCategory() throws Exception {
        // given
        RegularMissionCreationDto dto = new RegularMissionCreationDto("대중교통 이용 챌린지", 10L, "지하철 이용 후 인증샷 업로드",
                "test");

        // when & then
        mockMvc.perform(post("/api/admin/missions/regular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공: 관리자 계정이 마감 미션 조회에 성공한다.")
    void getDeadlineMission_success() throws Exception {
        // given
        Mission closedMission = missionRepository.save(
                Mission.builder()
                        .title("분리수거 미션")
                        .content("캔/플라스틱 분리배출 인증샷 올리기")
                        .missionPoint(10L)
                        .missionType(EVENT)
                        .startDate(LocalDate.now().minusDays(3))
                        .deadLine(LocalDate.now().minusDays(1))
                        .iconUrl("https://s3/icon.png")
                        .bannerUrl("https://s3/banner.png")
                        .status(CLOSED)
                        .category("publicTransportation")
                        .build()
        );

        missionParticipationRepository.save(
                builder()
                        .mission(closedMission)
                        .user(user1)
                        .participationStatus(
                                PENDING)
                        .build()
        );

        // when & then
        mockMvc.perform(get("/api/admin/missions/deadLine")
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    Assertions.assertThat(response).contains("분리수거 미션");
                    Assertions.assertThat(response).contains("1");
                });
    }

    @Test
    @DisplayName("실패: 일반 사용자가 마감 미션 조회를 시도하면 403 Forbidden 반환")
    void getDeadlineMission_fail_forbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/api/admin/missions/deadLine")
                        .with(user(new CustomUserDetails(user1)))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("성공: 마감 미션이 없을 경우 빈 리스트 반환")
    void getDeadlineMission_success_emptyList() throws Exception {
        // when & then
        mockMvc.perform(get("/api/admin/missions/deadLine")
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    Assertions.assertThat(response).isEqualTo("[]");
                });
    }

    @Test
    @DisplayName("성공: 관리자 계정이 종료 미션 조회에 성공한다.")
    void getTerminationMission_success() throws Exception {
        // given
        Mission closedMission = missionRepository.save(
                Mission.builder()
                        .title("에코백 챌린지")
                        .content("장 볼 때 에코백 인증샷 올리기")
                        .missionPoint(15L)
                        .missionType(EVENT)
                        .startDate(LocalDate.now().minusDays(5))
                        .deadLine(LocalDate.now().minusDays(1))
                        .iconUrl("https://s3/icon.png")
                        .bannerUrl("https://s3/banner.png")
                        .status(CLOSED)
                        .category("publicTransportation")
                        .build()
        );

        missionParticipationRepository.save(
                builder()
                        .mission(closedMission)
                        .user(user1)
                        .participationStatus(
                                APPROVED)
                        .build()
        );

        // when & then
        mockMvc.perform(get("/api/admin/missions/termination")
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    Assertions.assertThat(response).contains("에코백 챌린지");
                    Assertions.assertThat(response).contains("1");
                });
    }

    @Test
    @DisplayName("실패: 일반 사용자가 종료 미션 조회를 시도하면 403 Forbidden 반환")
    void getTerminationMission_fail_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/missions/termination")
                        .with(user(new CustomUserDetails(user1)))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("성공: 종료 미션이 없을 경우 빈 리스트 반환")
    void getTerminationMission_success_emptyList() throws Exception {
        mockMvc.perform(get("/api/admin/missions/termination")
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    Assertions.assertThat(response).isEqualTo("[]");
                });
    }


    @Test
    @DisplayName("성공: 관리자 계정이 승인 대기 미션 조회에 성공한다.")
    void getPendingMission_success() throws Exception {
        // given
        Mission openMission = missionRepository.save(
                Mission.builder()
                        .title("텀블러 사용 챌린지")
                        .content("일회용 컵 대신 텀블러 사용 인증샷 업로드")
                        .missionPoint(10L)
                        .missionType(EVENT)
                        .startDate(LocalDate.now().minusDays(2))
                        .deadLine(LocalDate.now().plusDays(2))
                        .iconUrl("https://s3/icon.png")
                        .bannerUrl("https://s3/banner.png")
                        .status(OPEN)
                        .category("publicTransportation")
                        .build()
        );

        missionParticipationRepository.save(
                builder()
                        .mission(openMission)
                        .user(user1)
                        .participationStatus(
                                PENDING)
                        .build()
        );

        // when & then
        mockMvc.perform(get("/api/admin/missions/pending")
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    Assertions.assertThat(response).contains("텀블러 사용 챌린지");
                    Assertions.assertThat(response).contains("1");
                });
    }

    @Test
    @DisplayName("실패: 일반 사용자가 승인 대기 미션 조회를 시도하면 403 Forbidden 반환")
    void getPendingMission_fail_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/missions/pending")
                        .with(user(new CustomUserDetails(user1)))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("성공: 관리자 계정이 카테고리 이름 리스트 조회에 성공한다.")
    void getCategoryNameList_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/admin/missions/category")
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    Assertions.assertThat(response).contains("PUBLIC_TRANSPORTATION");
                    Assertions.assertThat(response).contains("RECYCLING");
                });
    }

    @Test
    @DisplayName("실패: 일반 사용자가 카테고리 이름 리스트 조회를 시도하면 403 Forbidden 반환")
    void getCategoryNameList_fail_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/missions/category")
                        .with(user(new CustomUserDetails(user1)))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("성공: 관리자 계정이 특정 미션의 승인 요청 목록 조회에 성공한다.")
    void getApprovalRequestList_success() throws Exception {
        // given
        Mission mission = missionRepository.save(
                Mission.builder()
                        .title("텀블러 인증 챌린지")
                        .content("일회용 컵 대신 텀블러 사용 인증샷 업로드")
                        .missionPoint(10L)
                        .missionType(EVENT)
                        .startDate(LocalDate.now().minusDays(3))
                        .deadLine(LocalDate.now().plusDays(2))
                        .iconUrl("https://s3/icon.png")
                        .bannerUrl("https://s3/banner.png")
                        .status(OPEN)
                        .category("publicTransportation")
                        .build()
        );

        var participation = missionParticipationRepository.save(
                builder()
                        .mission(mission)
                        .user(user1)
                        .participationStatus(PENDING)
                        .build()
        );

        var file = fileRepository.save(
                ofParticipation(
                        participation,
                        s3Uploader.makeMetaData(new MockMultipartFile(
                                "testImage", "test.png", "image/png", "fake".getBytes()
                        ))
                )
        );

        s3Uploader.uploadFile(file.getFileKey(), new MockMultipartFile(
                "testImage", "test.png", "image/png", "fake".getBytes()
        ));

        mockMvc.perform(get("/api/admin/missions/request/{missionId}", mission.getId())
                        .with(user(new CustomUserDetails(admin)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    Assertions.assertThat(response).contains("텀블러 인증 챌린지");
                    Assertions.assertThat(response).contains("user");
                    Assertions.assertThat(response).contains("https://s3/icon.png");
                });

    }

    @Test
    @DisplayName("실패: 일반 사용자가 승인 요청 목록 조회를 시도하면 403 Forbidden 반환")
    void getApprovalRequestList_fail_forbidden() throws Exception {
        // given
        Mission mission = missionRepository.save(
                Mission.builder()
                        .title("일반 사용자 접근 테스트 미션")
                        .content("테스트용 미션")
                        .missionPoint(5L)
                        .missionType(EVENT)
                        .startDate(LocalDate.now())
                        .deadLine(LocalDate.now().plusDays(3))
                        .iconUrl("https://s3/icon.png")
                        .bannerUrl("https://s3/banner.png")
                        .status(OPEN)
                        .category("recycling")
                        .build()
        );

        // when & then
        mockMvc.perform(get("/api/admin/missions/request/{missionId}", mission.getId())
                        .with(user(new CustomUserDetails(user1)))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }


}