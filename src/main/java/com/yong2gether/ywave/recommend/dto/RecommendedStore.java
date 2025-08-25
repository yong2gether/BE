package com.yong2gether.ywave.recommend.dto;

public record RecommendedStore(
        Long id,
        String name,
        String roadAddr,
        String sigungu,
        double lng,
        double lat,
        String reason
) {}
