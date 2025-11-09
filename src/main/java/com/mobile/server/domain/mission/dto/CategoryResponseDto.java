package com.mobile.server.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "카테고리 DTO")
@Builder
public class CategoryResponseDto {
    @NotBlank
    @Schema(description = "카테고리 이름 -> 미션을 생성 시 카테고리 필드에 값을 넣을 때 사용")
    String categoryName;
}
