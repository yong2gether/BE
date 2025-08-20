package com.yong2gether.ywave.store.dto;

public class PopularStoreDto {
    private Long id;
    private String name;
    private String sigungu;
    private double lng;
    private double lat;
    private double distM;
    private String placeId;

    private Double rating;             // core.stores.rating
    private Integer userRatingsTotal;  // core.stores.user_ratings_total
    private Double popularityScore;    // core.stores.popularity_score
    private String category;

    // getters/setters
    public Long getId() { return id; } public void setId(Long id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public String getSigungu(){return sigungu;} public void setSigungu(String s){this.sigungu=s;}
    public Double getLng(){return lng;} public void setLng(Double d){this.lng=d;}
    public Double getLat(){return lat;} public void setLat(Double d){this.lat=d;}
    public Double getDistM(){return distM;} public void setDistM(Double m){this.distM=m;}
    public Double getRating(){return rating;} public void setRating(Double r){this.rating=r;}
    public String getPlaceId(){return placeId;} public void setPlaceId(String p){this.placeId=p;}
    public Integer getUserRatingsTotal(){return userRatingsTotal;} public void setUserRatingsTotal(Integer r){this.userRatingsTotal=r;}
    public Double getPopularityScore(){return popularityScore;}  public void setPopularityScore(Double s){this.popularityScore=s;}
    public String getCategory(){return category;} public void setCategory(String c){this.category=c;}
}
