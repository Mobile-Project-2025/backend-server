package com.mobile.server.domain.mission.controller;

import com.mobile.server.domain.auth.jwt.CustomUserDetails;
import com.mobile.server.domain.mission.dto.RegularMissionCreationDto;
import com.mobile.server.domain.mission.dto.dto.EventMissionCreationDto;
import com.mobile.server.domain.mission.service.MissionManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.web.bind.annotation.RequestBody;
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
            description = "관리자가 새로운 상시 미션을 생성한다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "상시 미션 생성 요청",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegularMissionCreationDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "정상적으로 생성됨.")
            }
    )
    @PostMapping(
            path = "/regular",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> createRegularMission(@AuthenticationPrincipal CustomUserDetails userInformation,
                                                     @RequestBody @Valid RegularMissionCreationDto mission) {
        managementService.createRegularMission(mission, userInformation.getUserId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(
            summary = "돌발 미션 생성",
            description = "관리자가 새로운 돌발 미션을 생성한다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "돌발 미션 생성 요청",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = EventMissionCreationDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "정상적으로 생성됨.")
            }
    )
    @PostMapping(
            path = "/event",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> createEventMission(@AuthenticationPrincipal CustomUserDetails userInformation,
                                                   @ModelAttribute @Valid EventMissionCreationDto mission) {
        managementService.createEventMission(mission, userInformation.getUserId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


}
