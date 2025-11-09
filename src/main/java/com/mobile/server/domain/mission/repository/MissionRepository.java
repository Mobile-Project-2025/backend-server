package com.mobile.server.domain.mission.repository;

import com.mobile.server.domain.mission.domain.Mission;
import com.mobile.server.domain.missionParticipation.eum.MissionParticipationStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {

    @Query("""
                SELECT m
                FROM MissionParticipation p
                JOIN p.mission m
                WHERE m.status = :missionStatus
                  AND p.participationStatus = :missionParticipationStatus
            """)
    List<Mission> findAllByMissionStatusAndMissionParticipationStatus(
            @Param("missionStatus") com.mobile.server.domain.mission.e.MissionStatus missionStatus,
            @Param("missionParticipationStatus") MissionParticipationStatus status);


    @Query("""
                SELECT m
                FROM MissionParticipation p
                JOIN p.mission m
                WHERE m.status = :missionStatus
                  AND p.participationStatus != :missionParticipationStatus
            """)
    List<Mission> findAllByMissionStatusAndMissionParticipationStatusNot(
            @Param("missionStatus") com.mobile.server.domain.mission.e.MissionStatus missionStatus,
            @Param("missionParticipationStatus") MissionParticipationStatus status);


    List<Mission> findAllByStatusAndDeadLineLessThanEqual(
            com.mobile.server.domain.mission.e.MissionStatus missionStatus,
            LocalDate deadLine);

    List<Mission> findAllByStatusAndStartDateEquals(
            com.mobile.server.domain.mission.e.MissionStatus missionStatus,
            LocalDate startDate);


}
