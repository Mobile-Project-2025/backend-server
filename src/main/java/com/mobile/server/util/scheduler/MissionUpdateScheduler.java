package com.mobile.server.util.scheduler;

import com.mobile.server.domain.mission.domain.Mission;
import com.mobile.server.domain.mission.repository.MissionRepository;
import com.mobile.server.domain.regularMission.RegularMissionRepository;
import com.mobile.server.domain.regularMission.domain.RegularMission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MissionUpdateScheduler {
    private final RegularMissionRepository regularMissionRepository;
    private final MissionRepository missionRepository;

    /*
    11시 30분에 상시 미션 목록의 상시 미션을 모두 미션 테이블 삽입
    11시 55분에 미션 목록 중 오늘 이전 날짜를 전부 가져와 마감이 아닌 일정에 대해 닫기 처리
    12시에 오늘이 미션 시작일인 미션에 대해 모두 Open 상태로 업데이트 처리
     */
    @Scheduled(cron = "0 30 23 * * ?")
    public void insertRegularMissionToMission() {
        int errorCount = 0;
        for (RegularMission regularMission : regularMissionRepository.findAll()) {
            try {
                missionRepository.save(Mission.makeMissionFromRegularMission(regularMission));
            } catch (Exception e) {
                log.warn("미션 생성 중 예외 발생: regularMissionId = {}, message = {}", regularMission.getId(), e.getMessage());
                errorCount++;
            }
            if (confirmSchedulerShutdown(errorCount)) {
                break;
            }
        }
    }

    private boolean confirmSchedulerShutdown(int errorCount) {
        if (errorCount >= 10) {
            log.error("오류 10회 발생- 스케줄러 시스템 점검 필요");
            return true;
        }
        return false;
    }

}
