package com.mobile.server.util.scheduler;

import com.mobile.server.domain.mission.domain.Mission;
import com.mobile.server.domain.mission.e.MissionStatus;
import com.mobile.server.domain.mission.repository.MissionRepository;
import com.mobile.server.domain.regularMission.RegularMissionRepository;
import com.mobile.server.domain.regularMission.domain.RegularMission;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Scheduled(cron = "0 55 23 * * ?")
    public void closeMissionDueToday() {
        List<Mission> missions = findMissionToBeCompleted();
        if (missions.isEmpty()) {
            log.info("[스케줄러] 오늘 마감된 미션이 없습니다.");
            return;
        }
        closeMissions(missions);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void openMissionStartToday() {
        List<Mission> missions = findMissionStartingToday();
        if (missions.isEmpty()) {
            log.info("[스케줄러] 오늘 열리는 미션이 없습니다.");
            return;
        }
        openMissions(missions);
    }


    private void openMissions(List<Mission> missions) {
        for (Mission mission : missions) {
            try {
                openSingleMission(mission);
            } catch (Exception e) {
                log.error("미션 ID={} 열기 실패: {}", mission.getId(), e.getMessage());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void openSingleMission(Mission mission) {
        mission.openMission();
        missionRepository.save(mission);
    }

    private List<Mission> findMissionStartingToday() {
        return missionRepository.findAllByStatusAndStartDateEquals(
                MissionStatus.CLOSED
                , LocalDate.now());
    }


    private void closeMissions(List<Mission> missions) {
        for (Mission mission : missions) {
            try {
                closeSingleMission(mission);
            } catch (Exception e) {
                log.error("미션 ID={} 닫기 실패: {}", mission.getId(), e.getMessage());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void closeSingleMission(Mission mission) {
        mission.closeMission();
        missionRepository.save(mission);
    }

    private List<Mission> findMissionToBeCompleted() {
        return missionRepository.findAllByStatusAndDeadLineLessThanEqual(
                MissionStatus.OPEN
                , LocalDate.now());
    }


    private boolean confirmSchedulerShutdown(int errorCount) {
        if (errorCount >= 10) {
            log.error("오류 10회 발생- 스케줄러 시스템 점검 필요");
            return true;
        }
        return false;
    }

}
