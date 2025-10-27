package com.mobile.server.domain.mission.e;

public enum MissionType {
    EVENT("돌발 미션"),
    SCHEDULED("상시 미션");

    private final String type;

    MissionType(String type) {
        this.type = type;
    }
}
