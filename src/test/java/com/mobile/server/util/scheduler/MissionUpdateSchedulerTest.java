package com.mobile.server.util.scheduler;

import com.mobile.server.domain.mission.domain.Mission;
import com.mobile.server.domain.mission.repository.MissionRepository;
import com.mobile.server.domain.regularMission.RegularMissionRepository;
import com.mobile.server.domain.regularMission.domain.RegularMission;
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
}