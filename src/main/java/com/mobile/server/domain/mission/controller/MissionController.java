package com.mobile.server.domain.mission.controller;

import com.mobile.server.domain.auth.jwt.CustomUserDetails;
import com.mobile.server.domain.mission.dto.MissionResponseDto;
import com.mobile.server.domain.mission.service.MissionManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/missions")
@Tag(name = "User Mission API", description = "유저 미션 조회 기능 제공")
@RequiredArgsConstructor
public class MissionController {
    private final MissionManagementService missionManagementService;

    @Operation(
            summary = "상시 미션 조회",
            description = "현재 진행 중인(OPEN 상태) 상시 미션 목록을 조회한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상적으로 조회됨.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MissionResponseDto.class))
                    )
            }
    )
    @GetMapping("/regular")
    public ResponseEntity<List<MissionResponseDto>> getScheduledMissions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MissionResponseDto> result = missionManagementService.getScheduledMissions(userDetails);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "돌발 미션 조회",
            description = "현재 진행 중인(OPEN 상태) 돌발 미션 목록을 조회한다. 참여 인원 수도 함께 제공된다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상적으로 조회됨.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MissionResponseDto.class))
                    )
            }
    )
    @GetMapping("/event")
    public ResponseEntity<List<MissionResponseDto>> getEventMissions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MissionResponseDto> result = missionManagementService.getEventMissions(userDetails);
        return ResponseEntity.ok(result);
    }
}

