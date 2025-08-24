package com.yong2gether.ywave.store.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yong2gether.ywave.store.config.PlacesProperties;
import com.yong2gether.ywave.store.dto.PlaceDetailsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PlacesClient {

    private final RestClient rest;
    private final PlacesProperties props;

    public PlacesClient(RestClient placesRestClient, PlacesProperties props) {
        this.rest = placesRestClient;
        this.props = props;
    }

    // --- Find Place (그대로) ---
    public String findPlaceIdByText(String input, double biasLng, double biasLat) {
        MultiValueMap<String, String> q = new LinkedMultiValueMap<>();
        q.add("input", input);
        q.add("inputtype", "textquery");
        q.add("fields", "place_id,name,geometry");
        q.add("locationbias", "point:" + biasLat + "," + biasLng);
        q.add("language", props.getLanguage());
        q.add("key", props.getApiKey());

        FindPlaceResponse resp = rest.get()
                .uri(uri -> uri.path("/findplacefromtext/json").queryParams(q).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(FindPlaceResponse.class);

        if (resp == null || !"OK".equals(resp.status) || resp.candidates == null || resp.candidates.isEmpty()) {
            if (resp != null) {
                log.warn("[Places Find] status={} (no candidates)", resp.status);
            } else {
                log.warn("[Places Find] null response");
            }
            return null;
        }
        return resp.candidates.get(0).placeId;
    }

    // --- Place Details (개선) ---
    private static final String FULL_FIELDS = String.join(",",
            "place_id","name","formatted_address","international_phone_number",
            "opening_hours","photos","reviews",
            "rating","geometry","website","url","user_ratings_total");

    private static final String BASIC_FIELDS = String.join(",",
            "place_id","name","formatted_address","international_phone_number",
            "rating","geometry","website","url","user_ratings_total");

    public PlaceDetailsDto getPlaceDetails(String placeId) {
        // 1차: FULL
        DetailsResponse full = callDetails(placeId, FULL_FIELDS);
        if (!isOk(full)) {
            log.warn("[Places Details] FULL failed: status={} error={} placeId={}",
                    full != null ? full.status : "null",
                    full != null ? full.errorMessage : "null",
                    placeId);

            // 2차: BASIC 재시도
            DetailsResponse basic = callDetails(placeId, BASIC_FIELDS);
            if (!isOk(basic)) {
                log.warn("[Places Details] BASIC failed: status={} error={} placeId={}",
                        basic != null ? basic.status : "null",
                        basic != null ? basic.errorMessage : "null",
                        placeId);
                return null; // 서비스에서 404 처리
            }
            return mapToDto(basic.result);
        }
        return mapToDto(full.result);
    }

    private DetailsResponse callDetails(String placeId, String fields) {
        MultiValueMap<String, String> q = new LinkedMultiValueMap<>();
        q.add("place_id", placeId);
        q.add("fields", fields);
        q.add("language", props.getLanguage());
        q.add("key", props.getApiKey());

        try {
            return rest.get()
                    .uri(uri -> uri.path("/details/json").queryParams(q).build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(DetailsResponse.class);
        } catch (Exception e) {
            log.error("[Places Details] HTTP error placeId={} fields={}", placeId, fields, e);
            return null;
        }
    }

    private boolean isOk(DetailsResponse resp) {
        return resp != null && "OK".equals(resp.status) && resp.result != null;
    }

    private PlaceDetailsDto mapToDto(DetailsResponse.Result r) {
        if (r == null) return null;

        Double lng = (r.geometry != null && r.geometry.location != null) ? r.geometry.location.lng : null;
        Double lat = (r.geometry != null && r.geometry.location != null) ? r.geometry.location.lat : null;

        List<PlaceDetailsDto.Photo> photos = new ArrayList<>();
        if (r.photos != null) {
            for (DetailsResponse.Photo p : r.photos) {
                String url = buildPhotoUrl(p.photoReference);
                photos.add(new PlaceDetailsDto.Photo(url, p.width, p.height));
            }
        }

        List<PlaceDetailsDto.Review> reviews = new ArrayList<>();
        if (r.reviews != null) {
            for (DetailsResponse.Review rv : r.reviews) {
                // 네 DTO가 (author, rating, text, time, photos) 생성자를 가진 것으로 보였음
                reviews.add(new PlaceDetailsDto.Review(rv.authorName, rv.rating, rv.text, rv.time, List.of()));
            }
        }

        List<String> weekdayText = (r.openingHours != null) ? r.openingHours.weekdayText : null;

        PlaceDetailsDto dto = new PlaceDetailsDto(
                r.placeId, r.name, r.formattedAddress, r.internationalPhoneNumber,
                lng, lat, r.website, r.url, r.rating,
                weekdayText, photos, reviews
        );
        dto.setReviewCount(r.userRatingsTotal);
        return dto;
    }

    public String buildPhotoUrl(String photoReference) {
        if (photoReference == null) return null;
        return props.getBaseUrl().replace("/place","") + "/place/photo"
                + "?maxwidth=" + props.getPhotoMaxwidth()
                + "&photo_reference=" + photoReference
                + "&key=" + props.getApiKey();
    }

    // --- 내부 응답 DTOs ---
    public static class FindPlaceResponse {
        public List<Candidate> candidates;
        public String status;
        public static class Candidate {
            @JsonProperty("place_id")
            public String placeId;
        }
    }

    public static class DetailsResponse {
        public String status;
        @JsonProperty("error_message")
        public String errorMessage;
        public Result result;

        public static class Result {
            @JsonProperty("place_id")
            public String placeId;
            public String name;
            @JsonProperty("formatted_address")
            public String formattedAddress;
            @JsonProperty("international_phone_number")
            public String internationalPhoneNumber;

            @JsonProperty("opening_hours")
            public OpeningHours openingHours;

            public List<Photo> photos;
            public List<Review> reviews;
            public Double rating;
            public Geometry geometry;
            public String website;
            @JsonProperty("url")
            public String url;
            @JsonProperty("user_ratings_total")
            public Integer userRatingsTotal;
        }

        public static class OpeningHours {
            @JsonProperty("weekday_text")
            public List<String> weekdayText;
        }
        public static class Photo {
            @JsonProperty("photo_reference")
            public String photoReference;
            public Integer width;
            public Integer height;
        }
        public static class Review {
            @JsonProperty("author_name")
            public String authorName;
            public Double rating;
            public String text;
            public Long time;
        }
        public static class Geometry {
            public Location location;
        }
        public static class Location {
            public Double lat;
            public Double lng;
        }
    }
}
