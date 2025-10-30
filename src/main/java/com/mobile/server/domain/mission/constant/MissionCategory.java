package com.mobile.server.domain.mission.constant;

public enum MissionCategory {
    PUBLIC_TRANSPORTATION("대중교통"),
    ETC("기타"),
    RECYCLING("재활용"),
    TUMBLER("텀블러");


    private final String category;

    MissionCategory(String category) {
        this.category = category;
    }
}
