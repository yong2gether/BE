package com.yong2gether.ywave.mypage.dto;

import java.util.List;

public record BookmarkGroupDetailDto(
    Long groupId,
    String groupName,
    String iconUrl,
    Boolean isDefault,
    List<Long> stores
) {}
