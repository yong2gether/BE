// src/main/java/com/yong2gether/ywave/mypage/dto/BookmarkGroupDto.java
package com.yong2gether.ywave.mypage.dto;

import java.util.List;

public record BookmarkGroupDto(
        Long groupId,
        String groupName,
        boolean isDefault,
        String iconUrl,
        List<BookmarkedStoreDto> stores
) {}
