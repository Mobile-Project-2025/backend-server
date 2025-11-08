package com.mobile.server.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "미션 승인 요청 목록 DTO")
@Builder
@Setter
@Getter
public class ApprovalRequestResponseDto {

    @Schema(description = "미션 제목")
    @NotBlank
    String title;

    @Schema(description = "미션 설명")
    String content;

    @Schema(description = "아이콘 URL")
    @NotBlank
    String iconImageUrl;

    @Schema(description = "미션 시작 날짜")
    @NotNull
    @PastOrPresent
    LocalDate startDate;

    @Schema(description = "미션 종료 날짜")
    @NotNull
    LocalDate deadLine;

    @Schema(description = " 현재까지 참여한 인원 수 (돌발 미션인 경우에만 해당 필드 존재)")
    @PositiveOrZero
    Integer participationCount;

    @Schema(description = "미션 포인트")
    @NotNull
    @Positive
    Long missionPoint;

    @Schema(description = "미션 승인 요청자 목록")
    List<RequesterDto> requesterList;


}
