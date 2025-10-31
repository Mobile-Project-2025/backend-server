package com.mobile.server.domain.missionParticipation.repository;

import com.mobile.server.domain.missionParticipation.domain.MissionParticipation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionParticipationRepository extends JpaRepository<MissionParticipation, Long> {

    public List<MissionParticipation> findAllByMission_Id(Long missionId);
}
