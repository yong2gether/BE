package com.yong2gether.ywave.user.controller;

import com.yong2gether.ywave.auth.dto.LoginRequest;
import com.yong2gether.ywave.auth.dto.LoginResponse;
import com.yong2gether.ywave.user.dto.SignUpRequest;
import com.yong2gether.ywave.user.dto.SignUpResponse;
import com.yong2gether.ywave.user.dto.UserProfileResponse;
import com.yong2gether.ywave.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임을 입력하면 계정이 생성되는 API입니다.")
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signup(@RequestBody SignUpRequest request) {
        SignUpResponse response = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "로그인", description = "실제 처리는 LoginFilter에서 수행되는 API입니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        throw new IllegalStateException("스웨거 docs에서 띄우는 용도입니다.");
    }

}
