// src/main/java/com/yong2gether/ywave/mypage/dto/BookmarkedStoreDto.java
package com.yong2gether.ywave.mypage.dto;

public record BookmarkedStoreDto(
        Long storeId,
        String storeName,
        String category,
        String roadAddress,
        Double lat,
        Double lng,
        String phone,
        Double rating,
        Integer reviewCount,
        String thumbnailUrl
) {}
