package com.mobile.server.domain.mission.dto;

import com.mobile.server.domain.mission.e.MissionType;
import com.mobile.server.domain.missionParticipation.eum.MissionParticipationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "미션 참여 이력 상세 조회 응답 DTO")
public class ParticipationHistoryDetailDto {

    @Schema(description = "미션 참여 ID", example = "15")
    private Long participationId;

    @Schema(description = "미션 ID", example = "3")
    private Long missionId;

    @Schema(description = "미션 제목", example = "텀블러 사용하기")
    private String title;

    @Schema(description = "미션 내용/설명", example = "개인 텀블러를 사용하여 일회용 컵 사용을 줄여주세요.")
    private String content;

    @Schema(description = "미션 배너 이미지 URL", example = "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png")
    private String bannerUrl;

    @Schema(description = "미션 아이콘 이미지 URL", example = "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/de7b9a05-1d2f-4588-8835-db6fd8593f3c.png")
    private String iconUrl;

    @Schema(description = "미션 성공 시 획득 포인트", example = "100")
    private Long missionPoint;

    @Schema(description = "해당 미션의 총 참여 수", example = "42")
    private Integer participationCount;

    @Schema(description = "미션 카테고리", example = "TUMBLER")
    private String category;

    @Schema(description = "미션 타입 (SCHEDULED: 상시, EVENT: 돌발)", example = "SCHEDULED")
    private MissionType missionType;

    @Schema(description = "참여 상태 (APPROVED: 승인됨, REJECTED: 반려됨)", example = "APPROVED")
    private MissionParticipationStatus participationStatus;

    @Schema(description = "제출한 사진 URL (없으면 null)", example = "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/participations/abc123.jpg")
    private String submittedPhotoUrl;

    @Schema(description = "참여(제출) 일시", example = "2025-11-20T14:30:00")
    private LocalDateTime participatedAt;

    @Schema(description = "미션 시작일", example = "2025-11-01")
    private LocalDate startDate;

    @Schema(description = "미션 마감일", example = "2025-11-30")
    private LocalDate deadLine;
}

