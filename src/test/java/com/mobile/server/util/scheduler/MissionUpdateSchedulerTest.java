package com.mobile.server.util.scheduler;

import static com.mobile.server.domain.mission.e.MissionStatus.CLOSED;
import static com.mobile.server.domain.mission.e.MissionStatus.OPEN;

import com.mobile.server.domain.mission.constant.MissionCategory;
import com.mobile.server.domain.mission.domain.Mission;
import com.mobile.server.domain.mission.e.MissionType;
import com.mobile.server.domain.mission.repository.MissionRepository;
import com.mobile.server.domain.regularMission.RegularMissionRepository;
import com.mobile.server.domain.regularMission.domain.RegularMission;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MissionUpdateSchedulerTest {
    @Autowired
    private MissionUpdateScheduler missionUpdateScheduler;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private RegularMissionRepository regularMissionRepository;

    @Test
    @DisplayName("정상적인 RegularMission 목록이 저장되는지 테스트")
    void insertRegularMissionToMission_successCase() {
        RegularMission mission1 = RegularMission.builder().title("title1").missionPoint(30L)
                .iconUrl("url").bannerUrl("url")
                .category("category").build();
        RegularMission mission2 = RegularMission.builder().title("title2").missionPoint(30L)
                .iconUrl("url").bannerUrl("url")
                .category("category").build();
        List<RegularMission> regularMissions = List.of(mission1, mission2);
        regularMissionRepository.saveAll(regularMissions);

        missionUpdateScheduler.insertRegularMissionToMission();

        List<Mission> savedMissions = missionRepository.findAll();

        Assertions.assertThat(savedMissions.size()).isEqualTo(regularMissions.size());
        Assertions.assertThat(savedMissions)
                .extracting(Mission::getTitle)
                .containsExactlyInAnyOrder("title1", "title2");

    }

    @Test
    @DisplayName("오늘 마감된 OPEN 상태의 미션은 CLOSED로 변경된다")
    void closeMissionDueToday_successCase() {
        // given
        Mission mission1 = Mission.builder()
                .title("오늘 마감 미션1")
                .content("content1")
                .missionPoint(10L)
                .missionType(MissionType.SCHEDULED)
                .category(MissionCategory.PUBLIC_TRANSPORTATION.name())
                .iconUrl("url")
                .bannerUrl("url")
                .status(OPEN)
                .startDate(LocalDate.now())
                .deadLine(LocalDate.now())
                .build();

        Mission mission2 = Mission.builder()
                .title("내일 마감 미션")
                .content("content2")
                .missionPoint(20L)
                .missionType(MissionType.SCHEDULED)
                .category(MissionCategory.PUBLIC_TRANSPORTATION.name())
                .startDate(LocalDate.now())
                .iconUrl("url")
                .bannerUrl("url")
                .status(OPEN)
                .deadLine(LocalDate.now().plusDays(1))
                .build();

        Mission mission3 = Mission.builder()
                .title("이미 닫힌 미션")
                .content("content3")
                .missionPoint(30L)
                .missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.now().minusDays(3))
                .category(MissionCategory.PUBLIC_TRANSPORTATION.name())
                .iconUrl("url")
                .bannerUrl("url")
                .status(CLOSED)
                .deadLine(LocalDate.now().minusDays(3))
                .build();

        missionRepository.saveAll(List.of(mission1, mission2, mission3));

        // when
        missionUpdateScheduler.closeMissionDueToday();

        // then
        List<Mission> all = missionRepository.findAll();

        // 오늘 마감된 미션1은 닫혀야 함
        Assertions.assertThat(all.stream()
                        .filter(m -> m.getTitle().equals("오늘 마감 미션1"))
                        .findFirst().get().getStatus())
                .isEqualTo(CLOSED);

        // 내일 마감된 미션은 여전히 OPEN
        Assertions.assertThat(all.stream()
                        .filter(m -> m.getTitle().equals("내일 마감 미션"))
                        .findFirst().get().getStatus())
                .isEqualTo(OPEN);

        // 이미 CLOSED였던 미션은 그대로
        Assertions.assertThat(all.stream()
                        .filter(m -> m.getTitle().equals("이미 닫힌 미션"))
                        .findFirst().get().getStatus())
                .isEqualTo(CLOSED);
    }


}