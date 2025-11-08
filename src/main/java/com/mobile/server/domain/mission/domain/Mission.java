package com.mobile.server.domain.mission.domain;

import com.mobile.server.domain.common.BaseCreatedEntity;
import com.mobile.server.domain.mission.dto.MissionResponseDto;
import com.mobile.server.domain.mission.e.MissionStatus;
import com.mobile.server.domain.mission.e.MissionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Mission extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String content;

    @Column(nullable = false)
    private Long missionPoint;

    @Getter
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private MissionType missionType;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate deadLine;

    @Column(nullable = false)
    private String iconUrl;

    @Column(nullable = false)
    private String bannerUrl;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private MissionStatus status;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Integer participationCount = 0;

    public void incrementParticipationCount() {
        this.participationCount += 1;
    }


    public MissionResponseDto makeMissionResponseDto() {
        return MissionResponseDto.builder().missionId(id)
                .title(title).missionPoint(missionPoint)
                .category(category).iconImageUrl(iconUrl)
                .bannerImageUrl(bannerUrl).createdAt(getCreatedAt()).build();
    }

}
