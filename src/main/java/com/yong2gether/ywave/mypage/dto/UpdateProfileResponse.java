package com.yong2gether.ywave.mypage.dto;

import com.yong2gether.ywave.user.service.UserService.ProfileUpdateResult;

public record UpdateProfileResponse(
    String message,
    String nickname
) {
    public static UpdateProfileResponse success(String nickname, String password, ProfileUpdateResult result) {
        StringBuilder message = new StringBuilder("프로필이 성공적으로 변경되었습니다.");
        
        if (result.isNicknameChanged()) {
            message.append(" 닉네임: ").append(nickname);
        }
        if (result.isPasswordChanged()) {
            message.append(" 비밀번호: 변경됨");
        }
        
        // 아무것도 변경되지 않은 경우
        if (!result.isNicknameChanged() && !result.isPasswordChanged()) {
            message.append(" (변경된 내용 없음)");
        }
        
        return new UpdateProfileResponse(message.toString(), nickname);
    }
}
