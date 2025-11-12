package com.mobile.server.domain.mission.controller;

import com.mobile.server.domain.auth.entity.RoleType;
import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.auth.jwt.CustomUserDetails;
import com.mobile.server.domain.auth.repository.UserRepository;
import com.mobile.server.domain.mission.domain.Mission;
import com.mobile.server.domain.mission.e.MissionStatus;
import com.mobile.server.domain.mission.e.MissionType;
import com.mobile.server.domain.mission.repository.MissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}

