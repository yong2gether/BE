package com.yong2gether.ywave.store.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.places")
public class PlacesProperties {
    private String apiKey;
    private String baseUrl = "https://maps.googleapis.com/maps/api/place";
    private String language = "ko";
    private Integer photoMaxwidth = 1600;

    public String getApiKey() { return apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public String getLanguage() { return language; }
    public Integer getPhotoMaxwidth() { return photoMaxwidth; }

    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public void setLanguage(String language) { this.language = language; }
    public void setPhotoMaxwidth(Integer photoMaxwidth) { this.photoMaxwidth = photoMaxwidth; }
}
