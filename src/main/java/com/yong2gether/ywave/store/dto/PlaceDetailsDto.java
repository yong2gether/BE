package com.yong2gether.ywave.store.dto;

import java.util.List;

public class PlaceDetailsDto {

    public static class Photo {
        private String url;
        private Integer width;
        private Integer height;

        public Photo() {}
        public Photo(String url, Integer width, Integer height) {
            this.url = url; this.width = width; this.height = height;
        }
        public String getUrl() { return url; }
        public Integer getWidth() { return width; }
        public Integer getHeight() { return height; }
        public void setUrl(String url) { this.url = url; }
        public void setWidth(Integer width) { this.width = width; }
        public void setHeight(Integer height) { this.height = height; }
    }

    public static class Review {
        private String authorName;
        private Double rating;
        private String text;
        private Long time; // epoch seconds

        public Review() {}
        public Review(String authorName, Double rating, String text, Long time) {
            this.authorName = authorName; this.rating = rating; this.text = text; this.time = time;
        }
        public String getAuthorName() { return authorName; }
        public Double getRating() { return rating; }
        public String getText() { return text; }
        public Long getTime() { return time; }
        public void setAuthorName(String authorName) { this.authorName = authorName; }
        public void setRating(Double rating) { this.rating = rating; }
        public void setText(String text) { this.text = text; }
        public void setTime(Long time) { this.time = time; }
    }

    private String placeId;
    private String name;
    private String formattedAddress;
    private String internationalPhoneNumber;
    private Double lng;
    private Double lat;
    private String website;
    private String googleMapsUri; // url
    private Double rating;
    private List<String> weekdayText; // 영업시간
    private List<Photo> photos;
    private List<Review> reviews;

    public PlaceDetailsDto() {}

    public PlaceDetailsDto(String placeId, String name, String formattedAddress, String internationalPhoneNumber,
                           Double lng, Double lat, String website, String googleMapsUri, Double rating,
                           List<String> weekdayText, List<Photo> photos, List<Review> reviews) {
        this.placeId = placeId; this.name = name; this.formattedAddress = formattedAddress;
        this.internationalPhoneNumber = internationalPhoneNumber; this.lng = lng; this.lat = lat;
        this.website = website; this.googleMapsUri = googleMapsUri; this.rating = rating;
        this.weekdayText = weekdayText; this.photos = photos; this.reviews = reviews;
    }

    public String getPlaceId() { return placeId; }
    public String getName() { return name; }
    public String getFormattedAddress() { return formattedAddress; }
    public String getInternationalPhoneNumber() { return internationalPhoneNumber; }
    public Double getLng() { return lng; }
    public Double getLat() { return lat; }
    public String getWebsite() { return website; }
    public String getGoogleMapsUri() { return googleMapsUri; }
    public Double getRating() { return rating; }
    public List<String> getWeekdayText() { return weekdayText; }
    public List<Photo> getPhotos() { return photos; }
    public List<Review> getReviews() { return reviews; }

    public void setPlaceId(String placeId) { this.placeId = placeId; }
    public void setName(String name) { this.name = name; }
    public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
    public void setInternationalPhoneNumber(String internationalPhoneNumber) { this.internationalPhoneNumber = internationalPhoneNumber; }
    public void setLng(Double lng) { this.lng = lng; }
    public void setLat(Double lat) { this.lat = lat; }
    public void setWebsite(String website) { this.website = website; }
    public void setGoogleMapsUri(String googleMapsUri) { this.googleMapsUri = googleMapsUri; }
    public void setRating(Double rating) { this.rating = rating; }
    public void setWeekdayText(List<String> weekdayText) { this.weekdayText = weekdayText; }
    public void setPhotos(List<Photo> photos) { this.photos = photos; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
}
