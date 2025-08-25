package com.yong2gether.ywave.store.repository.projection;

public interface StorePickRow {
    Long getId();
    String getName();
    String getSigungu();
    String getRoadAddr();
    Double getLng();
    Double getLat();
    Double getPopularityScore();
}