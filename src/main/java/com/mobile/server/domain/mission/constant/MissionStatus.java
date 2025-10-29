package com.mobile.server.domain.mission.e;

public enum MissionStatus {
    OPEN("열림"),
    CLOSED("닫힘");

    private final String status;

    MissionStatus(String status) {
        this.status = status;
    }
}
