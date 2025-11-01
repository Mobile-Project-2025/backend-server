package com.mobile.server.domain.mission.dto.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "마감 미션 응답 DTO")
public class MissionResponseDto {

    @Schema(description = "미션 id")
    @NotNull
    Long missionId;

    @Schema(description = "미션 제목(필수)")
    @NotBlank
    String title;

    @Schema(description = "미션 포인트(필수), 양수")
    @NotNull
    @Positive
    Long missionPoint;

    @Schema(description = "미션 카테고리")
    @NotBlank
    String category;

    @Schema(description = "아이콘 URL")
    @NotBlank
    String iconImageUrl;

    @Schema(description = "배너 URL")
    @NotBlank
    String bannerImageUrl;

    @Schema(description = " 현재까지 참여한 인원 수 (돌발 미션인 경우에만 해당 필드 존재)")
    @PositiveOrZero
    Integer participationCount;

    @Schema(description = "미션 생성 날짜")
    @NotNull
    @PastOrPresent
    LocalDateTime createdAt;


}
