package com.mobile.server.domain.mission.controller;

import com.mobile.server.config.FakeS3Uploader;
import com.mobile.server.domain.auth.entity.RoleType;
import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.auth.jwt.CustomUserDetails;
import com.mobile.server.domain.auth.repository.UserRepository;
import com.mobile.server.domain.mission.domain.Mission;
import com.mobile.server.domain.mission.e.MissionStatus;
import com.mobile.server.domain.mission.e.MissionType;
import com.mobile.server.domain.mission.repository.MissionRepository;
import com.mobile.server.domain.missionParticipation.domain.MissionParticipation;
import com.mobile.server.domain.missionParticipation.eum.MissionParticipationStatus;
import com.mobile.server.domain.missionParticipation.repository.MissionParticipationRepository;
import org.junit.jupiter.api.AfterEach;
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

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class MissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MissionParticipationRepository missionParticipationRepository;

    @Autowired
    private FakeS3Uploader fakeS3Uploader;

    private User testUser;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .studentId("20251112")
                .password("password")
                .nickname("테스트유저")
                .role(RoleType.STUDENT)
                .build();
        testUser = userRepository.save(testUser);
        userDetails = new CustomUserDetails(testUser);
    }

    @AfterEach
    void tearDown() {
        fakeS3Uploader.clearStorage();
    }

    @Test
    @DisplayName("상시 미션 조회 성공")
    void getScheduledMissions_Success() throws Exception {
        // given
        Mission mission1 = Mission.builder()
                .title("상시 미션 1")
                .content("상시 미션 내용 1")
                .missionPoint(100L)
                .missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.now())
                .deadLine(LocalDate.now().plusDays(7))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/de7b9a05-1d2f-4588-8835-db6fd8593f3c.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png")
                .status(MissionStatus.OPEN)
                .category("TUMBLER")
                .build();

        Mission mission2 = Mission.builder()
                .title("상시 미션 2")
                .content("상시 미션 내용 2")
                .missionPoint(200L)
                .missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.now())
                .deadLine(LocalDate.now().plusDays(7))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/d02cd5a5-4469-4efb-bf7e-1191a3594383.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png")
                .status(MissionStatus.OPEN)
                .category("RECYCLING")
                .build();

        missionRepository.save(mission1);
        missionRepository.save(mission2);

        // when & then
        mockMvc.perform(get("/api/missions/regular")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("상시 미션 1"))
                .andExpect(jsonPath("$[0].missionPoint").value(100))
                .andExpect(jsonPath("$[1].title").value("상시 미션 2"))
                .andExpect(jsonPath("$[1].missionPoint").value(200));
    }

    @Test
    @DisplayName("돌발 미션 조회 성공")
    void getEventMissions_Success() throws Exception {
        // given
        Mission mission = Mission.builder()
                .title("돌발 미션 1")
                .content("돌발 미션 내용 1")
                .missionPoint(300L)
                .missionType(MissionType.EVENT)
                .startDate(LocalDate.now())
                .deadLine(LocalDate.now().plusDays(3))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/2351f119-f70f-461e-b552-abdb621cffe1.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/537500d1-4fe2-4f06-9cf3-38e46ed87d64.png")
                .status(MissionStatus.OPEN)
                .category("ETC")
                .participationCount(10)
                .build();

        missionRepository.save(mission);

        // when & then
        mockMvc.perform(get("/api/missions/event")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("돌발 미션 1"))
                .andExpect(jsonPath("$[0].missionPoint").value(300))
                .andExpect(jsonPath("$[0].participationCount").value(10));
    }

    @Test
    @DisplayName("CLOSED 상태 상시 미션은 조회되지 않음")
    void getScheduledMissions_OnlyOpenStatus() throws Exception {
        // given
        Mission openMission = Mission.builder()
                .title("OPEN 상시 미션")
                .content("내용")
                .missionPoint(100L)
                .missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.now())
                .deadLine(LocalDate.now().plusDays(7))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/fc7b54f9-6360-4d31-87a1-7151d7099c39.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png")
                .status(MissionStatus.OPEN)
                .category("PUBLIC_TRANSPORTATION")
                .build();

        Mission closedMission = Mission.builder()
                .title("CLOSED 상시 미션")
                .content("내용")
                .missionPoint(200L)
                .missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.now().minusDays(7))
                .deadLine(LocalDate.now().minusDays(1))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/d02cd5a5-4469-4efb-bf7e-1191a3594383.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png")
                .status(MissionStatus.CLOSED)
                .category("RECYCLING")
                .build();

        missionRepository.save(openMission);
        missionRepository.save(closedMission);

        // when & then
        mockMvc.perform(get("/api/missions/regular")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("OPEN 상시 미션"));
    }

    @Test
    @DisplayName("CLOSED 상태 돌발 미션은 조회되지 않음")
    void getEventMissions_OnlyOpenStatus() throws Exception {
        // given
        Mission openMission = Mission.builder()
                .title("OPEN 돌발 미션")
                .content("내용")
                .missionPoint(300L)
                .missionType(MissionType.EVENT)
                .startDate(LocalDate.now())
                .deadLine(LocalDate.now().plusDays(3))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/de7b9a05-1d2f-4588-8835-db6fd8593f3c.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/537500d1-4fe2-4f06-9cf3-38e46ed87d64.png")
                .status(MissionStatus.OPEN)
                .category("TUMBLER")
                .participationCount(5)
                .build();

        Mission closedMission = Mission.builder()
                .title("CLOSED 돌발 미션")
                .content("내용")
                .missionPoint(400L)
                .missionType(MissionType.EVENT)
                .startDate(LocalDate.now().minusDays(3))
                .deadLine(LocalDate.now().minusDays(1))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/2351f119-f70f-461e-b552-abdb621cffe1.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/537500d1-4fe2-4f06-9cf3-38e46ed87d64.png")
                .status(MissionStatus.CLOSED)
                .category("ETC")
                .participationCount(10)
                .build();

        missionRepository.save(openMission);
        missionRepository.save(closedMission);

        // when & then
        mockMvc.perform(get("/api/missions/event")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("OPEN 돌발 미션"))
                .andExpect(jsonPath("$[0].participationCount").value(5));
    }

    @Test
    @DisplayName("미션 상세 조회 성공 - 제출하지 않은 상시 미션")
    void getMissionDetail_NotSubmitted_Success() throws Exception {
        // given
        Mission mission = Mission.builder()
                .title("텀블러 사용하기")
                .content("개인 텀블러를 사용하여 일회용 컵 사용을 줄여주세요.")
                .missionPoint(100L)
                .missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.of(2025, 11, 18))
                .deadLine(LocalDate.of(2025, 11, 25))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/de7b9a05-1d2f-4588-8835-db6fd8593f3c.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png")
                .status(MissionStatus.OPEN)
                .category("TUMBLER")
                .build();

        Mission savedMission = missionRepository.save(mission);

        // when & then
        mockMvc.perform(get("/api/missions/" + savedMission.getId())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missionId").value(savedMission.getId()))
                .andExpect(jsonPath("$.title").value("텀블러 사용하기"))
                .andExpect(jsonPath("$.content").value("개인 텀블러를 사용하여 일회용 컵 사용을 줄여주세요."))
                .andExpect(jsonPath("$.missionPoint").value(100))
                .andExpect(jsonPath("$.category").value("TUMBLER"))
                .andExpect(jsonPath("$.startDate").value("2025-11-18"))
                .andExpect(jsonPath("$.deadLine").value("2025-11-25"))
                .andExpect(jsonPath("$.missionType").value("SCHEDULED"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.participationCount").doesNotExist())
                .andExpect(jsonPath("$.hasSubmitted").value(false));
    }

    @Test
    @DisplayName("미션 상세 조회 성공 - 제출한 상시 미션")
    void getMissionDetail_AlreadySubmitted_Success() throws Exception {
        // given
        Mission mission = Mission.builder()
                .title("분리수거하기")
                .content("재활용품을 올바르게 분리배출해주세요.")
                .missionPoint(150L)
                .missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.of(2025, 11, 18))
                .deadLine(LocalDate.of(2025, 11, 30))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/d02cd5a5-4469-4efb-bf7e-1191a3594383.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png")
                .status(MissionStatus.OPEN)
                .category("RECYCLING")
                .build();

        Mission savedMission = missionRepository.save(mission);

        // 미션 제출 기록 생성
        MissionParticipation participation = MissionParticipation.builder()
                .mission(savedMission)
                .user(testUser)
                .participationStatus(MissionParticipationStatus.PENDING)
                .build();
        missionParticipationRepository.save(participation);

        // when & then
        mockMvc.perform(get("/api/missions/" + savedMission.getId())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missionId").value(savedMission.getId()))
                .andExpect(jsonPath("$.title").value("분리수거하기"))
                .andExpect(jsonPath("$.hasSubmitted").value(true));
    }

    @Test
    @DisplayName("미션 상세 조회 성공 - 돌발 미션 (participationCount 포함)")
    void getMissionDetail_EventMission_Success() throws Exception {
        // given
        Mission mission = Mission.builder()
                .title("캠퍼스 클린업 이벤트")
                .content("캠퍼스 내 쓰레기를 주워주세요.")
                .missionPoint(300L)
                .missionType(MissionType.EVENT)
                .startDate(LocalDate.of(2025, 11, 18))
                .deadLine(LocalDate.of(2025, 11, 20))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/2351f119-f70f-461e-b552-abdb621cffe1.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/537500d1-4fe2-4f06-9cf3-38e46ed87d64.png")
                .status(MissionStatus.OPEN)
                .category("ETC")
                .participationCount(25)
                .build();

        Mission savedMission = missionRepository.save(mission);

        // when & then
        mockMvc.perform(get("/api/missions/" + savedMission.getId())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missionId").value(savedMission.getId()))
                .andExpect(jsonPath("$.title").value("캠퍼스 클린업 이벤트"))
                .andExpect(jsonPath("$.missionType").value("EVENT"))
                .andExpect(jsonPath("$.participationCount").value(25))
                .andExpect(jsonPath("$.hasSubmitted").value(false));
    }

    @Test
    @DisplayName("미션 상세 조회 실패 - 존재하지 않는 미션")
    void getMissionDetail_NotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/api/missions/99999")
                        .with(user(userDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("미션 상세 조회 실패 - STUDENT 권한 없음")
    void getMissionDetail_Forbidden() throws Exception {
        // given
        User adminUser = User.builder()
                .studentId("20251119")
                .password("password")
                .nickname("관리자")
                .role(RoleType.ADMIN)
                .build();
        adminUser = userRepository.save(adminUser);
        CustomUserDetails adminDetails = new CustomUserDetails(adminUser);

        Mission mission = Mission.builder()
                .title("테스트 미션")
                .content("내용")
                .missionPoint(100L)
                .missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.of(2025, 11, 18))
                .deadLine(LocalDate.of(2025, 11, 25))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/de7b9a05-1d2f-4588-8835-db6fd8593f3c.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png")
                .status(MissionStatus.OPEN)
                .category("TUMBLER")
                .build();

        Mission savedMission = missionRepository.save(mission);

        // when & then
        mockMvc.perform(get("/api/missions/" + savedMission.getId())
                        .with(user(adminDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("미션 제출 성공 - 상시 미션")
    void submitMission_Scheduled_Success() throws Exception {
        // given
        Mission mission = Mission.builder()
                .title("텀블러 사용하기")
                .content("개인 텀블러를 사용하여 일회용 컵 사용을 줄여주세요.")
                .missionPoint(100L)
                .missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.of(2025, 11, 19))
                .deadLine(LocalDate.of(2025, 11, 26))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/de7b9a05-1d2f-4588-8835-db6fd8593f3c.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png")
                .status(MissionStatus.OPEN)
                .category("TUMBLER")
                .build();

        Mission savedMission = missionRepository.save(mission);

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/missions/" + savedMission.getId() + "/submit")
                        .file(photo)
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participationId").exists())
                .andExpect(jsonPath("$.message").value("미션이 성공적으로 제출되었습니다."))
                .andExpect(jsonPath("$.submittedAt").exists());
    }

    @Test
    @DisplayName("미션 제출 성공 - 돌발 미션")
    void submitMission_Event_Success() throws Exception {
        // given
        Mission mission = Mission.builder()
                .title("캠퍼스 클린업 이벤트")
                .content("캠퍼스 내 쓰레기를 주워주세요.")
                .missionPoint(300L)
                .missionType(MissionType.EVENT)
                .startDate(LocalDate.of(2025, 11, 19))
                .deadLine(LocalDate.of(2025, 11, 21))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/2351f119-f70f-461e-b552-abdb621cffe1.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/537500d1-4fe2-4f06-9cf3-38e46ed87d64.png")
                .status(MissionStatus.OPEN)
                .category("ETC")
                .participationCount(0)
                .build();

        Mission savedMission = missionRepository.save(mission);

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "event-photo.png",
                "image/png",
                "event photo content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/missions/" + savedMission.getId() + "/submit")
                        .file(photo)
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participationId").exists())
                .andExpect(jsonPath("$.message").value("미션이 성공적으로 제출되었습니다."))
                .andExpect(jsonPath("$.submittedAt").exists());
    }

    @Test
    @DisplayName("미션 제출 실패 - 이미 제출한 미션")
    void submitMission_AlreadySubmitted_Fail() throws Exception {
        // given
        Mission mission = Mission.builder()
                .title("분리수거하기")
                .content("재활용품을 올바르게 분리배출해주세요.")
                .missionPoint(150L)
                .missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.of(2025, 11, 19))
                .deadLine(LocalDate.of(2025, 11, 30))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/d02cd5a5-4469-4efb-bf7e-1191a3594383.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png")
                .status(MissionStatus.OPEN)
                .category("RECYCLING")
                .build();

        Mission savedMission = missionRepository.save(mission);

        // 이미 제출한 기록 생성
        MissionParticipation participation = MissionParticipation.builder()
                .mission(savedMission)
                .user(testUser)
                .participationStatus(MissionParticipationStatus.PENDING)
                .build();
        missionParticipationRepository.save(participation);

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "duplicate.jpg",
                "image/jpeg",
                "duplicate image content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/missions/" + savedMission.getId() + "/submit")
                        .file(photo)
                        .with(user(userDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("미션 제출 실패 - 마감된 미션")
    void submitMission_ClosedMission_Fail() throws Exception {
        // given
        Mission mission = Mission.builder()
                .title("마감된 미션")
                .content("마감된 미션입니다.")
                .missionPoint(100L)
                .missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.of(2025, 11, 10))
                .deadLine(LocalDate.of(2025, 11, 18))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/de7b9a05-1d2f-4588-8835-db6fd8593f3c.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png")
                .status(MissionStatus.CLOSED)
                .category("TUMBLER")
                .build();

        Mission savedMission = missionRepository.save(mission);

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "late.jpg",
                "image/jpeg",
                "late image content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/missions/" + savedMission.getId() + "/submit")
                        .file(photo)
                        .with(user(userDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("미션 제출 실패 - 존재하지 않는 미션")
    void submitMission_NotFound_Fail() throws Exception {
        // given
        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "notfound.jpg",
                "image/jpeg",
                "notfound image content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/missions/99999/submit")
                        .file(photo)
                        .with(user(userDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("미션 제출 실패 - STUDENT 권한 없음")
    void submitMission_Forbidden() throws Exception {
        // given
        User adminUser = User.builder()
                .studentId("20251120")
                .password("password")
                .nickname("관리자")
                .role(RoleType.ADMIN)
                .build();
        adminUser = userRepository.save(adminUser);
        CustomUserDetails adminDetails = new CustomUserDetails(adminUser);

        Mission mission = Mission.builder()
                .title("테스트 미션")
                .content("내용")
                .missionPoint(100L)
                .missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.of(2025, 11, 19))
                .deadLine(LocalDate.of(2025, 11, 26))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/de7b9a05-1d2f-4588-8835-db6fd8593f3c.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png")
                .status(MissionStatus.OPEN)
                .category("TUMBLER")
                .build();

        Mission savedMission = missionRepository.save(mission);

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "admin-test.jpg",
                "image/jpeg",
                "admin test image content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/missions/" + savedMission.getId() + "/submit")
                        .file(photo)
                        .with(user(adminDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("승인 대기 미션 목록 조회 성공")
    void getPendingMissions_Success() throws Exception {
        // given
        Mission mission1 = Mission.builder()
                .title("텀블러 사용하기")
                .content("텀블러를 사용하세요")
                .missionPoint(100L)
                .missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.of(2025, 11, 19))
                .deadLine(LocalDate.of(2025, 11, 26))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/de7b9a05-1d2f-4588-8835-db6fd8593f3c.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png")
                .status(MissionStatus.OPEN)
                .category("TUMBLER")
                .build();

        Mission mission2 = Mission.builder()
                .title("캠퍼스 클린업")
                .content("캠퍼스를 깨끗하게")
                .missionPoint(300L)
                .missionType(MissionType.EVENT)
                .startDate(LocalDate.of(2025, 11, 19))
                .deadLine(LocalDate.of(2025, 11, 21))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/2351f119-f70f-461e-b552-abdb621cffe1.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/537500d1-4fe2-4f06-9cf3-38e46ed87d64.png")
                .status(MissionStatus.OPEN)
                .category("ETC")
                .participationCount(0)
                .build();

        Mission savedMission1 = missionRepository.save(mission1);
        Mission savedMission2 = missionRepository.save(mission2);

        // 미션 제출 (PENDING 상태)
        MissionParticipation participation1 = MissionParticipation.builder()
                .mission(savedMission1)
                .user(testUser)
                .participationStatus(MissionParticipationStatus.PENDING)
                .build();
        MissionParticipation savedParticipation1 = missionParticipationRepository.save(participation1);

        MissionParticipation participation2 = MissionParticipation.builder()
                .mission(savedMission2)
                .user(testUser)
                .participationStatus(MissionParticipationStatus.PENDING)
                .build();
        MissionParticipation savedParticipation2 = missionParticipationRepository.save(participation2);

        // when & then
        mockMvc.perform(get("/api/missions/pending")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].participationId").value(savedParticipation2.getId()))
                .andExpect(jsonPath("$[0].missionId").value(savedMission2.getId()))
                .andExpect(jsonPath("$[0].title").value("캠퍼스 클린업"))
                .andExpect(jsonPath("$[0].missionPoint").value(300))
                .andExpect(jsonPath("$[0].category").value("ETC"))
                .andExpect(jsonPath("$[0].missionType").value("EVENT"))
                .andExpect(jsonPath("$[0].participationStatus").value("PENDING"))
                .andExpect(jsonPath("$[1].participationId").value(savedParticipation1.getId()))
                .andExpect(jsonPath("$[1].missionId").value(savedMission1.getId()))
                .andExpect(jsonPath("$[1].title").value("텀블러 사용하기"))
                .andExpect(jsonPath("$[1].missionType").value("SCHEDULED"));
    }

    @Test
    @DisplayName("승인 대기 미션 목록 조회 - 빈 목록")
    void getPendingMissions_EmptyList() throws Exception {
        // when & then
        mockMvc.perform(get("/api/missions/pending")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("승인 대기 미션 목록 조회 - APPROVED 상태는 조회되지 않음")
    void getPendingMissions_OnlyPendingStatus() throws Exception {
        // given
        Mission mission = Mission.builder()
                .title("분리수거하기")
                .content("분리수거를 해주세요")
                .missionPoint(150L)
                .missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.of(2025, 11, 19))
                .deadLine(LocalDate.of(2025, 11, 30))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/d02cd5a5-4469-4efb-bf7e-1191a3594383.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png")
                .status(MissionStatus.OPEN)
                .category("RECYCLING")
                .build();

        Mission savedMission = missionRepository.save(mission);

        // PENDING 상태
        MissionParticipation pendingParticipation = MissionParticipation.builder()
                .mission(savedMission)
                .user(testUser)
                .participationStatus(MissionParticipationStatus.PENDING)
                .build();
        missionParticipationRepository.save(pendingParticipation);

        // APPROVED 상태 (조회되면 안됨)
        Mission approvedMission = Mission.builder()
                .title("승인된 미션")
                .content("승인된 미션")
                .missionPoint(200L)
                .missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.of(2025, 11, 19))
                .deadLine(LocalDate.of(2025, 11, 30))
                .iconUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/d02cd5a5-4469-4efb-bf7e-1191a3594383.png")
                .bannerUrl("https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png")
                .status(MissionStatus.OPEN)
                .category("RECYCLING")
                .build();
        Mission savedApprovedMission = missionRepository.save(approvedMission);

        MissionParticipation approvedParticipation = MissionParticipation.builder()
                .mission(savedApprovedMission)
                .user(testUser)
                .participationStatus(MissionParticipationStatus.APPROVED)
                .build();
        missionParticipationRepository.save(approvedParticipation);

        // when & then
        mockMvc.perform(get("/api/missions/pending")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("분리수거하기"))
                .andExpect(jsonPath("$[0].participationStatus").value("PENDING"));
    }

    @Test
    @DisplayName("승인 대기 미션 목록 조회 실패 - STUDENT 권한 없음")
    void getPendingMissions_Forbidden() throws Exception {
        // given
        User adminUser = User.builder()
                .studentId("20251121")
                .password("password")
                .nickname("관리자")
                .role(RoleType.ADMIN)
                .build();
        adminUser = userRepository.save(adminUser);
        CustomUserDetails adminDetails = new CustomUserDetails(adminUser);

        // when & then
        mockMvc.perform(get("/api/missions/pending")
                        .with(user(adminDetails)))
                .andExpect(status().isForbidden());
    }
}
