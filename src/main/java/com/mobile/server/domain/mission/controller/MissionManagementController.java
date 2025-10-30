package com.mobile.server.domain.mission.controller;

import com.mobile.server.domain.auth.jwt.CustomUserDetails;
import com.mobile.server.domain.mission.dto.RegularMissionCreationDto;
import com.mobile.server.domain.mission.service.MissionManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/missions")
@Tag(name = "Admin API", description = "관리자 미션 관리 기능 제공")
@RequiredArgsConstructor
public class MissionManagementController {
    private final MissionManagementService managementService;

    @Operation(
            summary = "상시 미션 생성",
            description = "관리자가 새로운 상시 미션을 생성한다. (multipart/form-data 형식으로 이미지(필수x)와 함께 전송)",
            requestBody = @RequestBody(
                    required = true,
                    description = "상시 미션 생성 요청 (multipart/form-data)",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = RegularMissionCreationDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "정상적으로 생성됨.")
            }
    )
    @PostMapping(
            path = "/regular",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> createRegularMission(@AuthenticationPrincipal CustomUserDetails userInformation,
                                                     @ModelAttribute @Valid RegularMissionCreationDto mission) {
        managementService.createRegularMission(mission, userInformation.getUserId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
