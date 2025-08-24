package com.yong2gether.ywave.mypage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBookmarkGroupRequest(
        @Size(min = 1, max = 100, message = "그룹 이름은 1자 이상 100자 이하여야 합니다.")
        String groupName,  // 선택적 필드 (null이면 수정하지 않음)
        
        String iconUrl     // 선택적 필드 (null이면 수정하지 않음)
) {}
