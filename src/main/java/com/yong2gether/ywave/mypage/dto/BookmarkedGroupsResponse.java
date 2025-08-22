// src/main/java/com/yong2gether/ywave/mypage/dto/BookmarkedGroupsResponse.java
package com.yong2gether.ywave.mypage.dto;

import java.util.List;

public record BookmarkedGroupsResponse(
        String message,
        List<BookmarkGroupDto> groups
) {
    public static BookmarkedGroupsResponse ok(List<BookmarkGroupDto> groups) {
        return new BookmarkedGroupsResponse("북마크한 가맹점 목록 조회 성공", groups);
    }
}
