package com.yong2gether.ywave.store.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yong2gether.ywave.store.config.PlacesProperties;
import com.yong2gether.ywave.store.dto.PlaceDetailsDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class PlacesClient {

    private final RestClient rest;
    private final PlacesProperties props;

    public PlacesClient(RestClient placesRestClient, PlacesProperties props) {
        this.rest = placesRestClient;
        this.props = props;
    }

    // 텍스트 q로 가맹점 찾기
    public String findPlaceIdByText(String input, double biasLng, double biasLat) {
        MultiValueMap<String, String> q = new LinkedMultiValueMap<>();
        q.add("input", input);
        q.add("inputtype", "textquery");
        q.add("fields", "place_id,name,geometry");
        q.add("locationbias", "point:" + biasLat + "," + biasLng); // lat,lng
        q.add("language", props.getLanguage());
        q.add("key", props.getApiKey());

        FindPlaceResponse resp = rest.get()
                .uri(uri -> uri.path("/findplacefromtext/json").queryParams(q).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(FindPlaceResponse.class);

        if (resp == null || !"OK".equals(resp.status) || resp.candidates == null || resp.candidates.isEmpty()) {
            return null;
        }
        return resp.candidates.get(0).placeId;
    }

    // 가맹점 상세 정보 가져오기
    public PlaceDetailsDto getPlaceDetails(String placeId) {
        MultiValueMap<String, String> q = new LinkedMultiValueMap<>();
        q.add("place_id", placeId);
        q.add("fields",
                "place_id,name,formatted_address,international_phone_number,opening_hours,photos,reviews," +
                        "rating,geometry,website,url,user_ratings_total");
        q.add("language", props.getLanguage());
        q.add("key", props.getApiKey());

        DetailsResponse resp = rest.get()
                .uri(uri -> uri.path("/details/json").queryParams(q).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(DetailsResponse.class);

        if (resp == null || resp.result == null) return null;

        DetailsResponse.Result r = resp.result;

        Double lng = r.geometry != null && r.geometry.location != null ? r.geometry.location.lng : null;
        Double lat = r.geometry != null && r.geometry.location != null ? r.geometry.location.lat : null;

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
                reviews.add(new PlaceDetailsDto.Review(rv.authorName, rv.rating, rv.text, rv.time));
            }
        }

        List<String> weekdayText = r.openingHours != null ? r.openingHours.weekdayText : null;

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
        // 이 URL은 프론트에서 <img src>로 바로 사용 가능
        return props.getBaseUrl().replace("/place","") + "/place/photo"
                + "?maxwidth=" + props.getPhotoMaxwidth()
                + "&photo_reference=" + photoReference
                + "&key=" + props.getApiKey();
    }

    // 내부 응답

    public static class FindPlaceResponse {
        public List<Candidate> candidates;
        public String status;

        public static class Candidate {
            @JsonProperty("place_id")
            public String placeId;
        }
    }

    public static class DetailsResponse {
        public Result result;

        public static class Result {
            @JsonProperty("place_id")
            public String placeId;
            public String name;
            @JsonProperty("formatted_address")
            public String formattedAddress;
            @JsonProperty("international_phone_number")
            public String internationalPhoneNumber;
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
