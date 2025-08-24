package com.yong2gether.ywave.mypage.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Size(min = 2, max = 20, message = "닉네임은 2-20자 사이여야 합니다.")
    String nickname,
    
    @Size(min = 8, max = 100, message = "비밀번호는 8-100자 사이여야 합니다.")
    String password
) {}
