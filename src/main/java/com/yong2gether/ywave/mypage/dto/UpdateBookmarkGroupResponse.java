package com.yong2gether.ywave.mypage.dto;

public record UpdateBookmarkGroupResponse(
        String message,
        UpdatedBookmarkGroupDto group
) {
    public static UpdateBookmarkGroupResponse ok(UpdatedBookmarkGroupDto group) {
        return new UpdateBookmarkGroupResponse("북마크 그룹 수정 성공", group);
    }
}
