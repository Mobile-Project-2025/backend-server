package com.mobile.server.domain.auth.controller;

import com.mobile.server.domain.auth.dto.ProfileResponseDto;
import com.mobile.server.domain.auth.dto.UpdatePointReq;
import com.mobile.server.domain.auth.jwt.CustomUserDetails;
import com.mobile.server.domain.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "사용자", description = "사용자 프로필 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 닉네임과 보유 포인트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProfileResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "nickname": "홍길동",
                                      "cumulativePoint": 1500
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Unauthorized",
                                      "status": 401,
                                      "detail": "인증이 필요합니다.",
                                      "instance": "/api/users/me"
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
                                      "instance": "/api/users/me"
                                    }
                                    """)))
    })
    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDto> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProfileResponseDto profile = userService.getProfile(userDetails.getUserId());
        return ResponseEntity.ok(profile);
    }

    @Operation(summary = "사용자 포인트 수정", description = "특정 사용자의 포인트를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "포인트 수정 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 파라미터",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Bad Request",
                                      "status": 400,
                                      "detail": "유효하지 않은 파라미터가 포함되어 있습니다.",
                                      "instance": "/api/users/1/points",
                                      "point": "포인트는 0 이상이어야 합니다."
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
                                      "instance": "/api/users/1/points"
                                    }
                                    """)))
    })
    @PatchMapping("/{userId}/points")
    public ResponseEntity<String> updateUserPoint(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long userId,
            @Valid @RequestBody UpdatePointReq updatePointReq) {
        userService.updatePoint(userId, updatePointReq.getPoint());
        return ResponseEntity.ok("포인트 수정 성공");
    }
}

