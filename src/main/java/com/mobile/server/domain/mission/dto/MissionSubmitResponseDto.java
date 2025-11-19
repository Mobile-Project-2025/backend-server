package com.mobile.server.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "미션 제출 응답 DTO")
public class MissionSubmitResponseDto {

    @Schema(description = "미션 참여 ID")
    private Long participationId;

    @Schema(description = "응답 메시지")
    private String message;

    @Schema(description = "제출 시각")
    private LocalDateTime submittedAt;
}

