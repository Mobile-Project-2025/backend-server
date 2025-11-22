package com.mobile.server.domain.file.respository;

import com.mobile.server.domain.file.domain.File;
import com.mobile.server.domain.mission.dto.MissionParticipationFileDto;
import com.mobile.server.domain.missionParticipation.domain.MissionParticipation;
import com.mobile.server.domain.missionParticipation.eum.MissionParticipationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    @Query("""
                  SELECT NEW com.mobile.server.domain.mission.dto.
                  MissionParticipationFileDto(p.id,p.createdAt,p.user,f.fileKey)\s
                  FROM File f
                  JOIN f.participation p\s
                  WHERE p.mission.id = :missionId AND
                  p.participationStatus = :participationStatus AND f.isDeleted =false\s
            \s""")
    List<MissionParticipationFileDto> findAllByMission_IdAndMissionStatus(
            @Param("participationStatus") MissionParticipationStatus participationStatus,
            @Param("missionId") Long missionId
    );

    Optional<File> findByParticipationAndIsDeletedFalse(MissionParticipation participation);

}
