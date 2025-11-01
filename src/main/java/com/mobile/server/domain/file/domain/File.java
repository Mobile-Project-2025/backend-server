package com.mobile.server.domain.file.domain;

import com.mobile.server.domain.common.BaseCreatedEntity;
import com.mobile.server.domain.file.dto.FileDetailDto;
import com.mobile.server.domain.mission.domain.Mission;
import com.mobile.server.domain.missionParticipation.domain.MissionParticipation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

@Entity
@Check(constraints = """
        (
          (CASE WHEN mission_id IS NOT NULL THEN 1 ELSE 0 END) +
          (CASE WHEN mission_participation_id IS NOT NULL THEN 1 ELSE 0 END)
        ) <= 1
        """)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class File extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, unique = true, length = 512)
    private String fileKey;

    @Column(nullable = false, length = 100)
    private String fileType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_participation_id")
    private MissionParticipation participation;

    public void delete() {
        isDeleted = true;
    }

    public void deleteMissionFile() {
        isDeleted = true;
        mission = null;
    }

    public void deleteParticipationFile() {
        isDeleted = true;
        participation = null;
    }

    public static File ofFile(FileDetailDto fileDetail) {
        return base(fileDetail);
    }


    public static File ofMission(Mission mission, FileDetailDto fileDetail) {
        File f = base(fileDetail);
        f.mission = mission;
        return f;
    }

    public static File ofParticipation(MissionParticipation participation, FileDetailDto fileDetail) {
        File f = base(fileDetail);
        f.participation = participation;
        return f;
    }


    private static File base(FileDetailDto fileDetail) {
        File f = new File();
        f.fileName = fileDetail.getOriginalFileName();
        f.fileKey = fileDetail.getKey();
        f.fileType = fileDetail.getContentType();
        f.fileSize = fileDetail.getFileSize();
        return f;
    }
}
