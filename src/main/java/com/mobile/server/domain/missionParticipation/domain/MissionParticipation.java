package com.mobile.server.domain.missionParticipation.domain;

import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.common.BaseCreatedEntity;
import com.mobile.server.domain.mission.domain.Mission;
import com.mobile.server.domain.missionParticipation.eum.MissionParticipationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class MissionParticipation extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private MissionParticipationStatus participationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User user;


    public void approveParticipation(User participant, Long rewardPoint) {
        participationStatus = MissionParticipationStatus.APPROVED;
        participant.grantMissionPoint(rewardPoint);
    }

}
