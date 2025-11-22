package com.mobile.server.domain.mission.controller;

import com.mobile.server.domain.auth.jwt.CustomUserDetails;
import com.mobile.server.domain.mission.dto.MissionDetailDto;
import com.mobile.server.domain.mission.dto.MissionResponseDto;
import com.mobile.server.domain.mission.dto.MissionSubmitResponseDto;
import com.mobile.server.domain.mission.dto.ParticipationHistoryDto;
import com.mobile.server.domain.mission.dto.PendingMissionDto;
import com.mobile.server.domain.mission.service.MissionManagementService;
import com.mobile.server.domain.mission.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/missions")
@Tag(name = "User Mission API", description = "유저 미션 조회 기능 제공")
@RequiredArgsConstructor
public class MissionController {
    private final MissionManagementService missionManagementService;
    private final MissionService missionService;

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
                                    """)))
    })
    @GetMapping("/event")
    public ResponseEntity<List<MissionResponseDto>> getEventMissions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MissionResponseDto> result = missionManagementService.getEventMissions(userDetails);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "미션 상세 조회",
            description = "특정 미션의 상세 정보를 조회합니다. 현재 사용자의 제출 여부도 함께 반환됩니다. STUDENT 권한이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "미션 상세 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MissionDetailDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "missionId": 1,
                                      "title": "텀블러 사용하기",
                                      "content": "개인 텀블러를 사용하여 일회용 컵 사용을 줄여주세요.",
                                      "missionPoint": 100,
                                      "category": "TUMBLER",
                                      "iconImageUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/de7b9a05-1d2f-4588-8835-db6fd8593f3c.png",
                                      "bannerImageUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png",
                                      "startDate": "2025-11-18",
                                      "deadLine": "2025-11-25",
                                      "missionType": "SCHEDULED",
                                      "status": "OPEN",
                                      "participationCount": null,
                                      "hasSubmitted": false
                                    }
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
                                      "instance": "/api/missions/1"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "미션을 찾을 수 없음",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Not Found",
                                      "status": 404,
                                      "detail": "요청하신 미션은 존재하지 않습니다.",
                                      "instance": "/api/missions/999"
                                    }
                                    """)))
    })
    @GetMapping("/{missionId}")
    public ResponseEntity<MissionDetailDto> getMissionDetail(
            @Parameter(description = "미션 ID", required = true) @PathVariable Long missionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MissionDetailDto result = missionService.getMissionDetail(userDetails.getUserId(), missionId);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "미션 제출",
            description = "미션을 완료하고 인증 사진과 함께 제출합니다. STUDENT 권한이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "미션 제출 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MissionSubmitResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "participationId": 1,
                                      "message": "미션이 성공적으로 제출되었습니다.",
                                      "submittedAt": "2025-11-18T14:30:00"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (중복 제출, 마감된 미션 등)",
                    content = @Content(mediaType = "application/problem+json",
                            examples = {
                                    @ExampleObject(name = "중복 제출", value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Bad Request",
                                              "status": 400,
                                              "detail": "이미 제출한 미션입니다.",
                                              "instance": "/api/missions/1/submit"
                                            }
                                            """),
                                    @ExampleObject(name = "마감된 미션", value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Bad Request",
                                              "status": 400,
                                              "detail": "요청하신 미션은 이미 마감되었습니다.",
                                              "instance": "/api/missions/1/submit"
                                            }
                                            """)
                            })),
            @ApiResponse(responseCode = "403", description = "권한 없음 (STUDENT가 아닌 경우)",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Forbidden",
                                      "status": 403,
                                      "detail": "사용자는 해당 기능을 사용할 수 없습니다.",
                                      "instance": "/api/missions/1/submit"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "미션을 찾을 수 없음",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Not Found",
                                      "status": 404,
                                      "detail": "요청하신 미션은 존재하지 않습니다.",
                                      "instance": "/api/missions/999/submit"
                                    }
                                    """)))
    })
    @PostMapping(value = "/{missionId}/submit", consumes = "multipart/form-data")
    @RequestBody(content = @Content(mediaType = "multipart/form-data"))
    public ResponseEntity<MissionSubmitResponseDto> submitMission(
            @Parameter(description = "미션 ID", required = true) @PathVariable Long missionId,
            @Parameter(description = "인증 사진", required = true) @RequestPart("photo") MultipartFile photo,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MissionSubmitResponseDto result = missionService.submitMission(
                userDetails.getUserId(), missionId, photo);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "승인 대기 미션 목록 조회",
            description = "현재 사용자가 제출한 미션 중 승인 대기 중인(PENDING 상태) 미션 목록을 조회합니다. STUDENT 권한이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "승인 대기 미션 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PendingMissionDto.class)),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "participationId": 1,
                                        "missionId": 1,
                                        "title": "텀블러 사용하기",
                                        "missionPoint": 100,
                                        "category": "TUMBLER",
                                        "iconImageUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/de7b9a05-1d2f-4588-8835-db6fd8593f3c.png",
                                        "missionType": "SCHEDULED",
                                        "participationStatus": "PENDING",
                                        "submittedPhotoUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/participations/abc123.jpg",
                                        "submittedAt": "2025-11-19T14:30:00"
                                      },
                                      {
                                        "participationId": 2,
                                        "missionId": 3,
                                        "title": "캠퍼스 클린업 이벤트",
                                        "missionPoint": 300,
                                        "category": "ETC",
                                        "iconImageUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/2351f119-f70f-461e-b552-abdb621cffe1.png",
                                        "missionType": "EVENT",
                                        "participationStatus": "PENDING",
                                        "submittedPhotoUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/participations/def456.jpg",
                                        "submittedAt": "2025-11-19T10:15:00"
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
                                      "instance": "/api/missions/pending"
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
                                      "instance": "/api/missions/pending"
                                    }
                                    """)))
    })
    @GetMapping("/pending")
    public ResponseEntity<List<PendingMissionDto>> getPendingMissions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<PendingMissionDto> result = missionService.getPendingMissions(userDetails.getUserId());
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "과거 미션 참여 이력 조회",
            description = "유저가 참여한 과거 미션 목록을 조회합니다. 승인(APPROVED) 또는 반려(REJECTED)된 미션만 표시되며, 최신순으로 정렬됩니다. STUDENT 권한이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "과거 미션 참여 이력 조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ParticipationHistoryDto.class)),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "participationId": 15,
                                        "missionId": 3,
                                        "title": "텀블러 사용하기",
                                        "bannerUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png",
                                        "iconUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/de7b9a05-1d2f-4588-8835-db6fd8593f3c.png",
                                        "missionPoint": 100,
                                        "participationCount": 42,
                                        "participationStatus": "APPROVED",
                                        "participatedAt": "2025-11-20T14:30:00"
                                      },
                                      {
                                        "participationId": 12,
                                        "missionId": 5,
                                        "title": "대중교통 이용하기",
                                        "bannerUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/537500d1-4fe2-4f06-9cf3-38e46ed87d64.png",
                                        "iconUrl": "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/fc7b54f9-6360-4d31-87a1-7151d7099c39.png",
                                        "missionPoint": 150,
                                        "participationCount": 28,
                                        "participationStatus": "REJECTED",
                                        "participatedAt": "2025-11-18T09:15:00"
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
                                      "instance": "/api/missions/history"
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
                                      "instance": "/api/missions/history"
                                    }
                                    """)))
    })
    @GetMapping("/history")
    public ResponseEntity<List<ParticipationHistoryDto>> getParticipationHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ParticipationHistoryDto> result = missionService.getParticipationHistory(userDetails.getUserId());
        return ResponseEntity.ok(result);
    }
}
