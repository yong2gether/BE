package com.yong2gether.ywave.places.dto;

import java.util.List;

public record PlaceDetails(Double rating, Integer userRatingsTotal, List<PlacePhoto> photos) {}

