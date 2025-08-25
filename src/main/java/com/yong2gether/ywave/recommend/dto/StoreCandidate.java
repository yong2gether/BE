package com.yong2gether.ywave.recommend.dto;

public record StoreCandidate(
        Long id,
        String name,
        String roadAddr,
        String sigungu,
        double lng,
        double lat,
        double popularityScore,
        String reason
) {
    public StoreCandidate(Long id, String name, String roadAddr, String sigungu,
                          double lng, double lat, double popularityScore) {
        this(id, name, roadAddr, sigungu, lng, lat, popularityScore, null);
    }
}
