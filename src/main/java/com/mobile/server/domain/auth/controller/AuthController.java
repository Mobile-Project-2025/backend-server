package com.mobile.server.domain.auth.controller;

import com.mobile.server.domain.auth.dto.CheckDuplicationRes;
import com.mobile.server.domain.auth.dto.SignInReq;
import com.mobile.server.domain.auth.dto.SignInRes;
import com.mobile.server.domain.auth.dto.SignUpReq;
import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.auth.jwt.JWTService;
import com.mobile.server.domain.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "회원가입, 로그인, 로그아웃 등 인증 관련 API")
public class AuthController {

    private final UserService userService;
    private final JWTService jwtService;

    //회원가입
    @Operation(summary = "회원가입", description = "학번, 비밀번호, 닉네임을 입력받아 회원가입을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 파라미터",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Bad Request",
                                      "status": 400,
                                      "detail": "유효하지 않은 파라미터가 포함되어 있습니다.",
                                      "instance": "/api/auth/register",
                                      "studentId": "학번은 8자리 숫자여야 합니다.",
                                      "password": "비밀번호는 8자 이상이어야 합니다."
                                    }
                                    """))),
            @ApiResponse(responseCode = "409", description = "중복된 학번 또는 닉네임",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Conflict",
                                      "status": 409,
                                      "detail": "이미 사용 중인 학번입니다.",
                                      "instance": "/api/auth/register"
                                    }
                                    """)))
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody SignUpReq signInReq) {
        userService.register(signInReq);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
    }

    //학번 중복 체크
    @Operation(summary = "학번 중복 확인", description = "회원가입 시 학번 중복 여부를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용 가능한 학번"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 학번",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Conflict",
                                      "status": 409,
                                      "detail": "이미 사용 중인 학번입니다.",
                                      "instance": "/api/auth/check-student-id"
                                    }
                                    """)))
    })
    @GetMapping("/check-student-id")
    public ResponseEntity<CheckDuplicationRes> checkStudentId(
            @Parameter(description = "확인할 학번", required = true, example = "20251110")
            @RequestParam String studentId) {
        userService.checkStudentIdDuplication(studentId);
        return ResponseEntity.ok(new CheckDuplicationRes(true));
    }

    //닉네임 중복 체크
    @Operation(summary = "닉네임 중복 확인", description = "회원가입 시 닉네임 중복 여부를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용 가능한 닉네임"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 닉네임",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Conflict",
                                      "status": 409,
                                      "detail": "이미 사용 중인 닉네임입니다.",
                                      "instance": "/api/auth/check-nickname"
                                    }
                                    """)))
    })
    @GetMapping("/check-nickname")
    public ResponseEntity<CheckDuplicationRes> checkNickname(
            @Parameter(description = "확인할 닉네임", required = true, example = "홍길동")
            @RequestParam String nickname) {
        userService.checkNicknameDuplication(nickname);
        return ResponseEntity.ok(new CheckDuplicationRes(true));
    }

    //로그인
    @Operation(summary = "로그인", description = "학번과 비밀번호로 로그인하여 JWT 액세스 토큰을 발급받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 파라미터",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Bad Request",
                                      "status": 400,
                                      "detail": "유효하지 않은 파라미터가 포함되어 있습니다.",
                                      "instance": "/api/auth/login",
                                      "studentId": "학번은 8자리 숫자여야 합니다."
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "잘못된 학번 또는 비밀번호",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Unauthorized",
                                      "status": 401,
                                      "detail": "아이디 또는 비밀번호가 올바르지 않습니다.",
                                      "instance": "/api/auth/login"
                                    }
                                    """)))
    })
    @PostMapping("/login")
    public ResponseEntity<SignInRes> login(@Valid @RequestBody SignInReq signInReq) {
        User user = userService.verify(signInReq.getStudentId(), signInReq.getPassword());
        String access = jwtService.generateAccessToken(user);
        return ResponseEntity.ok(new SignInRes(access, user.getNickname(), user.getRole()));
    }

    //로그아웃
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃 처리합니다. (현재는 클라이언트 측에서 토큰 삭제 처리)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("로그아웃 성공");
    }

    //Refresh 토큰 (나중에 구현)
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Refresh not implemented yet");
    }
}
