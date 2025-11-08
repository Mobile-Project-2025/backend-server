package com.mobile.server.domain.mission.controller;

import com.mobile.server.domain.auth.jwt.CustomUserDetails;
import com.mobile.server.domain.mission.dto.ApprovalRequestResponseDto;
import com.mobile.server.domain.mission.dto.CategoryResponseDto;
import com.mobile.server.domain.mission.dto.EventMissionCreationDto;
import com.mobile.server.domain.mission.dto.MissionResponseDto;
import com.mobile.server.domain.mission.dto.RegularMissionCreationDto;
import com.mobile.server.domain.mission.service.MissionManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Operation(
            summary = "마감 미션 조회",
            description = "관리자가 마감되었지만 아직 승인 대기 중인 상태가 하나라도 남아있는 미션을 모두 조회한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상적으로 조회됨.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MissionResponseDto.class))
                    )
            }
    )
    @GetMapping(path = "/deadLine")
    public ResponseEntity<List<MissionResponseDto>> getDeadlineMission(
            @AuthenticationPrincipal CustomUserDetails userInformation) {
        List<MissionResponseDto> result = managementService.getDeadlineMission(
                userInformation.getUserId());
        return ResponseEntity.ok(result);
    }


    @Operation(
            summary = "종료 미션 조회",
            description = "관리자가 완전히 마감된 미션(마감 && 승인 대기x, 승인 혹은 반려 중 하나) 을 모두 조회한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상적으로 조회됨.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MissionResponseDto.class))
                    )
            }
    )
    @GetMapping(path = "/termination")
    public ResponseEntity<List<MissionResponseDto>> getTerminationMission(
            @AuthenticationPrincipal CustomUserDetails userInformation) {
        List<MissionResponseDto> result = managementService.getTerminationMission(
                userInformation.getUserId());
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "승인대기 미션 조회",
            description = "관리자가 마감되지 않은 승인대기 미션을 모두 조회한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상적으로 조회됨.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MissionResponseDto.class))
                    )
            }
    )
    @GetMapping(path = "/pending")
    public ResponseEntity<List<MissionResponseDto>> getPendingMission(
            @AuthenticationPrincipal CustomUserDetails userInformation) {
        List<MissionResponseDto> result = managementService.getPendingMission(userInformation.getUserId());
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "카테고리 조회",
            description = "미션에 대한 카테고리 목록을 모두 조회한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상적으로 조회됨.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CategoryResponseDto.class))
                    )
            }
    )
    @GetMapping(path = "/category")
    public ResponseEntity<List<CategoryResponseDto>> getCategoryNameList(
            @AuthenticationPrincipal CustomUserDetails userInformation) {
        List<CategoryResponseDto> result = managementService.getCategoryNameList(userInformation.getUserId());
        return ResponseEntity.ok(result);
    }


    @Operation(
            summary = "미션 승인 요청 목록 조회",
            description = "특정 미션의 승인 대기 중인 목록을 조회한다..",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상적으로 조회됨.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ApprovalRequestResponseDto.class))
                    )
            }
    )
    @GetMapping(path = "/request/{missionId}")
    public ResponseEntity<ApprovalRequestResponseDto> getApprovalRequestList(
            @AuthenticationPrincipal CustomUserDetails userInformation, @PathVariable String missionId) {
        ApprovalRequestResponseDto result = managementService.getApprovalRequestList(userInformation.getUserId(),
                missionId);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "미션 참여 승인 요청",
            description = "미션 참여에 대한 승인을 서버에 요청한다.(단건 요청)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상적으로 처리됨.")
            }
    )
    @PatchMapping(path = "/request/approve/{participationId}")
    public ResponseEntity<Void> requestMissionParticipationApprove(
            @AuthenticationPrincipal CustomUserDetails userInformation, @PathVariable String participationId) {
        managementService.requestMissionParticipationApprove(userInformation.getUserId(), participationId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
