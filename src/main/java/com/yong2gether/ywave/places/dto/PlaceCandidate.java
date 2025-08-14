package com.yong2gether.ywave.places.dto;

public record PlaceCandidate(
        String placeId, String name,
        Double rating, Integer userRatingsTotal,
        double lat, double lng,
        String address
) {}

