package com.mobile.server.domain.missionParticipation.repository;

import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.mission.domain.Mission;
import com.mobile.server.domain.missionParticipation.domain.MissionParticipation;
import com.mobile.server.domain.missionParticipation.eum.MissionParticipationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionParticipationRepository extends JpaRepository<MissionParticipation, Long> {

    Optional<MissionParticipation> findByMissionAndUser(Mission mission, User user);

    List<MissionParticipation> findByUserAndParticipationStatusOrderByCreatedAtDesc(
            User user, MissionParticipationStatus status);


    List<MissionParticipation> findByUserOrderByCreatedAtDesc(User user);

    Optional<MissionParticipation> findByIdAndUser(Long id, User user);

}
