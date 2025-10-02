package com.mobile.server.domain.auth.controller;

import com.mobile.server.domain.auth.dto.SignInReq;
import com.mobile.server.domain.auth.dto.SignInRes;
import com.mobile.server.domain.auth.dto.SignUpReq;
import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.auth.jwt.JWTService;
import com.mobile.server.domain.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JWTService jwtService;

    //회원가입
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody SignUpReq signInReq) {
        userService.register(signInReq);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //로그인
    @PostMapping("/login")
    public ResponseEntity<SignInRes> login(@RequestBody SignInReq signInReq) {
        User user = userService.verify(signInReq.getStudentId(), signInReq.getPassword());
        String access = jwtService.generateAccessToken(user);
        return ResponseEntity.ok(new SignInRes(access, user.getNickname(), user.getRole()));
    }

    //로그아웃
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
