package com.mobile.server.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "요청자 1명을 나타내는 DTO")
public class RequesterDto {

    @Schema(description = "미션 참여 id -> 승인 반려 처리 시 사용")
    @NotNull
    Long participationId;

    @Schema(description = "참여자 닉네임")
    @NotNull
    String nickName;

    @Schema(description = "미션 참여 사진 URL")
    @NotBlank
    String participationPhoto;

    @Schema(description = "미션 참여 시간 및 날짜")
    @PastOrPresent
    LocalDateTime missionParticipationTime;


}
