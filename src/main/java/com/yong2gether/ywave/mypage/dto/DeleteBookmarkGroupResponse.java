package com.yong2gether.ywave.mypage.dto;

public record DeleteBookmarkGroupResponse(
        String message,
        Long deletedGroupId
) {
    public static DeleteBookmarkGroupResponse ok(Long groupId) {
        return new DeleteBookmarkGroupResponse("북마크 그룹 삭제 성공", groupId);
    }
}


