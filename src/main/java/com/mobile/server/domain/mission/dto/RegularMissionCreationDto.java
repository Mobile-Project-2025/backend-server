package com.mobile.server.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "상시 미션 생성 요청 DTO")
@Setter
public class RegularMissionCreationDto {

    @Schema(description = "미션 제목(필수)")
    @NotBlank
    private String title;

    @Schema(description = "미션 포인트(필수), 양수")
    @NotNull
    @Positive
    private Long point;

    @Schema(description = "미션 설명(필수)")
    @NotBlank
    private String content;

    @Schema(description = "미션 카테고리")
    @NotBlank
    private String category;

}
