package com.mobile.server.domain.missionParticipation.eum;

public enum MissionParticipationStatus {
    PENDING("승인 대기"),
    APPROVED("승인"),
    REJECTED("반려");


    private final String status;

    MissionParticipationStatus(String status) {
        this.status = status;
    }


}
