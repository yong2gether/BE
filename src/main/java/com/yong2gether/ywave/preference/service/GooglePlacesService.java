package com.yong2gether.ywave.preference.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GooglePlacesService {

    @Value("${GOOGLE_PLACES_API_KEY}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public record LatLng(Double lat, Double lng) {}

    /**
     * Places Text Search로 place_id를 찾고, Place Details로 geometry.location(lat,lng)을 가져온다.
     * address 예: "경기도 용인시 수지구 죽전동" 또는 "경기도 용인시 수지구"
     */
    public Optional<LatLng> geocodeByPlaces(String sido, String sigungu, String dongOrNullOrAll) {
        try {
            String address = buildAddress(sido, sigungu, dongOrNullOrAll);

            // 1) Text Search
            URI textSearchUri = UriComponentsBuilder
                    .fromUriString("https://maps.googleapis.com/maps/api/place/textsearch/json")
                    .queryParam("query", address)
                    .queryParam("language", "ko")
                    .queryParam("region", "kr")
                    .queryParam("key", apiKey)
                    .build(true)
                    .toUri();

            ResponseEntity<Map> tsResp = restTemplate.getForEntity(textSearchUri, Map.class);
            if (!tsResp.getStatusCode().is2xxSuccessful() || tsResp.getBody() == null) return Optional.empty();

            var tsBody = tsResp.getBody();
            var tsStatus = (String) tsBody.get("status");
            if (tsStatus == null || !(tsStatus.equals("OK") || tsStatus.equals("ZERO_RESULTS"))) return Optional.empty();

            var results = (List<Map<String, Object>>) tsBody.get("results");
            if (results == null || results.isEmpty()) return Optional.empty();

            // 첫 결과 우선 (필요하면 타입 필터링 추가 가능)
            var first = results.get(0);
            var placeId = (String) first.get("place_id");
            if (placeId == null || placeId.isBlank()) return Optional.empty();

            // 2) Place Details (geometry.location만 요청)
            URI detailsUri = UriComponentsBuilder
                    .fromUriString("https://maps.googleapis.com/maps/api/place/details/json")
                    .queryParam("place_id", placeId)
                    .queryParam("fields", "geometry/location")
                    .queryParam("language", "ko")
                    .queryParam("key", apiKey)
                    .build(true)
                    .toUri();

            ResponseEntity<Map> detResp = restTemplate.getForEntity(detailsUri, Map.class);
            if (!detResp.getStatusCode().is2xxSuccessful() || detResp.getBody() == null) return Optional.empty();

            var detBody = detResp.getBody();
            var detStatus = (String) detBody.get("status");
            if (detStatus == null || !detStatus.equals("OK")) return Optional.empty();

            var result = (Map<String, Object>) detBody.get("result");
            if (result == null) return Optional.empty();

            var geometry = (Map<String, Object>) result.get("geometry");
            if (geometry == null) return Optional.empty();

            var location = (Map<String, Object>) geometry.get("location");
            if (location == null) return Optional.empty();

            Double lat = toDouble(location.get("lat"));
            Double lng = toDouble(location.get("lng"));
            if (lat == null || lng == null) return Optional.empty();

            return Optional.of(new LatLng(lat, lng));

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String buildAddress(String sido, String sigungu, String dong) {
        StringBuilder sb = new StringBuilder();
        if (sido != null && !sido.isBlank()) sb.append(sido.trim());
        if (sigungu != null && !sigungu.isBlank()) sb.append(" ").append(sigungu.trim());
        // 동이 null/빈문자/"전체"면 시군구 기준으로만 검색
        if (dong != null && !dong.isBlank() && !"전체".equals(dong.trim())) {
            sb.append(" ").append(dong.trim());
        }
        return sb.toString();
    }

    private Double toDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        if (o instanceof String s) try { return Double.parseDouble(s); } catch (Exception ignored) {}
        return null;
    }
}
