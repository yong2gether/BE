package com.yong2gether.ywave.user.controller;

import com.yong2gether.ywave.user.dto.EmailDuplicateRequest;
import com.yong2gether.ywave.user.dto.EmailDuplicateResponse;
import com.yong2gether.ywave.user.service.UserDuplicateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/duplicate")
public class UserDuplicateController {

    private final UserDuplicateService duplicateService;

    @PostMapping("/email")
    public ResponseEntity<EmailDuplicateResponse> checkEmail(
            @Valid @RequestBody EmailDuplicateRequest request
    ) {
        boolean duplicated = duplicateService.isEmailDuplicated(request.getEmail());
        String message = duplicated ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다.";
        return ResponseEntity.ok(new EmailDuplicateResponse(message, duplicated));
    }
}
