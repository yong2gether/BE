package com.yong2gether.ywave.mypage.dto;

public record CreateBookmarkGroupResponse(
        String message,
        CreatedBookmarkGroupDto group
) {
    public static CreateBookmarkGroupResponse ok(CreatedBookmarkGroupDto group) {
        return new CreateBookmarkGroupResponse("북마크 그룹 생성 성공", group);
    }
}



