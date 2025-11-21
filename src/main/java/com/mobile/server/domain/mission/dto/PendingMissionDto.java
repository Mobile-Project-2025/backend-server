package com.mobile.server.domain.mission.dto;

import com.mobile.server.domain.mission.e.MissionType;
import com.mobile.server.domain.missionParticipation.eum.MissionParticipationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "승인 대기 미션 응답 DTO")
public class PendingMissionDto {

    @Schema(description = "미션 참여 ID", example = "1")
    private Long participationId;

    @Schema(description = "미션 ID", example = "1")
    private Long missionId;

    @Schema(description = "미션 제목", example = "텀블러 사용하기")
    private String title;

    @Schema(description = "미션 포인트", example = "100")
    private Long missionPoint;

    @Schema(description = "미션 카테고리", example = "TUMBLER")
    private String category;

    @Schema(description = "아이콘 이미지 URL", example = "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/de7b9a05-1d2f-4588-8835-db6fd8593f3c.png")
    private String iconImageUrl;

    @Schema(description = "미션 타입 (SCHEDULED: 상시, EVENT: 돌발)", example = "SCHEDULED")
    private MissionType missionType;

    @Schema(description = "참여 상태 (PENDING: 승인대기, APPROVED: 승인됨, REJECTED: 거부됨)", example = "PENDING")
    private MissionParticipationStatus participationStatus;

    @Schema(description = "제출 일시", example = "2025-11-19T14:30:00")
    private LocalDateTime submittedAt;
}

