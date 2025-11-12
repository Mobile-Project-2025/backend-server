package com.mobile.server.domain.mission.controller;

import com.mobile.server.domain.auth.jwt.CustomUserDetails;
import com.mobile.server.domain.mission.dto.MissionResponseDto;
import com.mobile.server.domain.mission.service.MissionManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            description = "현재 진행 중인(OPEN 상태) 상시 미션 목록을 조회합니다. STUDENT 권한이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상시 미션 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MissionResponseDto.class)),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "missionId": 1,
                                        "title": "텀블러 사용하기",
                                        "missionPoint": 100,
                                        "category": "TUMBLER",
                                        "iconImageUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/de7b9a05-1d2f-4588-8835-db6fd8593f3c.png",
                                        "bannerImageUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png",
                                        "participationCount": null,
                                        "createdAt": "2025-11-12T10:00:00"
                                      },
                                      {
                                        "missionId": 2,
                                        "title": "분리수거하기",
                                        "missionPoint": 150,
                                        "category": "RECYCLING",
                                        "iconImageUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/d02cd5a5-4469-4efb-bf7e-1191a3594383.png",
                                        "bannerImageUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png",
                                        "participationCount": null,
                                        "createdAt": "2025-11-12T10:00:00"
                                      }
                                    ]
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음 (STUDENT가 아닌 경우)",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Forbidden",
                                      "status": 403,
                                      "detail": "사용자는 해당 기능을 사용할 수 없습니다.",
                                      "instance": "/api/missions/regular"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Not Found",
                                      "status": 404,
                                      "detail": "존재하지 않는 사용자입니다.",
                                      "instance": "/api/missions/regular"
                                    }
                                    """)))
    })
    @GetMapping("/regular")
    public ResponseEntity<List<MissionResponseDto>> getScheduledMissions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MissionResponseDto> result = missionManagementService.getScheduledMissions(userDetails);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "돌발 미션 조회",
            description = "현재 진행 중인(OPEN 상태) 돌발 미션 목록을 조회합니다. 참여 인원 수(participationCount)도 함께 제공됩니다. STUDENT 권한이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "돌발 미션 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MissionResponseDto.class)),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "missionId": 3,
                                        "title": "캠퍼스 클린업 이벤트",
                                        "missionPoint": 300,
                                        "category": "ETC",
                                        "iconImageUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/2351f119-f70f-461e-b552-abdb621cffe1.png",
                                        "bannerImageUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/537500d1-4fe2-4f06-9cf3-38e46ed87d64.png",
                                        "participationCount": 25,
                                        "createdAt": "2025-11-12T14:30:00"
                                      },
                                      {
                                        "missionId": 4,
                                        "title": "환경보호 챌린지",
                                        "missionPoint": 250,
                                        "category": "PUBLIC_TRANSPORTATION",
                                        "iconImageUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/fc7b54f9-6360-4d31-87a1-7151d7099c39.png",
                                        "bannerImageUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/537500d1-4fe2-4f06-9cf3-38e46ed87d64.png",
                                        "participationCount": 42,
                                        "createdAt": "2025-11-12T15:00:00"
                                      }
                                    ]
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음 (STUDENT가 아닌 경우)",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Forbidden",
                                      "status": 403,
                                      "detail": "사용자는 해당 기능을 사용할 수 없습니다.",
                                      "instance": "/api/missions/event"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Not Found",
                                      "status": 404,
                                      "detail": "존재하지 않는 사용자입니다.",
                                      "instance": "/api/missions/event"
                                    }
                                    """)))
    })
    @GetMapping("/event")
    public ResponseEntity<List<MissionResponseDto>> getEventMissions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MissionResponseDto> result = missionManagementService.getEventMissions(userDetails);
        return ResponseEntity.ok(result);
    }
}

