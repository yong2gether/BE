package com.yong2gether.ywave.store.dto;

public class NearbyStoreDto {
    private Long id;
    private String name;
    private String sigungu;
    private Double lng;
    private Double lat;
    private Double distM;
    private String placeId; // 있으면 바로 상세 이동 가능

    public NearbyStoreDto() {}

    public NearbyStoreDto(Long id, String name, String sigungu, Double lng, Double lat, Double distM, String placeId) {
        this.id = id; this.name = name; this.sigungu = sigungu;
        this.lng = lng; this.lat = lat; this.distM = distM; this.placeId = placeId;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSigungu() { return sigungu; }
    public Double getLng() { return lng; }
    public Double getLat() { return lat; }
    public Double getDistM() { return distM; }
    public String getPlaceId() { return placeId; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSigungu(String sigungu) { this.sigungu = sigungu; }
    public void setLng(Double lng) { this.lng = lng; }
    public void setLat(Double lat) { this.lat = lat; }
    public void setDistM(Double distM) { this.distM = distM; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }
}
