package com.yong2gether.ywave.mypage.dto;

public record BookmarkGroupDetailResponse(
    String message,
    BookmarkGroupDetailDto group
) {
    public static BookmarkGroupDetailResponse success(BookmarkGroupDetailDto group) {
        return new BookmarkGroupDetailResponse(
            "북마크 그룹 상세 조회 성공",
            group
        );
    }
}
