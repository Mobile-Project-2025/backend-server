package com.mobile.server.domain.mission.dto;

import com.mobile.server.domain.mission.e.MissionStatus;
import com.mobile.server.domain.mission.e.MissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "미션 상세 조회 응답 DTO")
public class MissionDetailDto {

    @Schema(description = "미션 ID")
    private Long missionId;

    @Schema(description = "미션 제목")
    private String title;

    @Schema(description = "미션 설명")
    private String content;

    @Schema(description = "미션 포인트")
    private Long missionPoint;

    @Schema(description = "미션 카테고리")
    private String category;

    @Schema(description = "아이콘 이미지 URL")
    private String iconImageUrl;

    @Schema(description = "배너 이미지 URL")
    private String bannerImageUrl;

    @Schema(description = "시작일")
    private LocalDate startDate;

    @Schema(description = "마감일")
    private LocalDate deadLine;

    @Schema(description = "미션 타입 (SCHEDULED: 상시, EVENT: 돌발)")
    private MissionType missionType;

    @Schema(description = "미션 상태 (OPEN: 진행중, CLOSED: 마감)")
    private MissionStatus status;

    @Schema(description = "참여자 수 (돌발 미션만)")
    private Integer participationCount;

    @Schema(description = "현재 사용자의 제출 여부")
    private Boolean hasSubmitted;
}

