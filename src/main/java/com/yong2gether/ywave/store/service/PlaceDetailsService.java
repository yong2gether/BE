package com.yong2gether.ywave.store.service;

import com.yong2gether.ywave.store.domain.Store;
import com.yong2gether.ywave.store.domain.StorePlaceMapping;
import com.yong2gether.ywave.store.dto.PlaceDetailsDto;
import com.yong2gether.ywave.store.repository.StorePlaceMappingRepository;
import com.yong2gether.ywave.store.repository.StoreRepository;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class PlaceDetailsService {

    private final StoreRepository storeRepository;
    private final StorePlaceMappingRepository mappingRepository;
    private final PlacesClient placesClient;

    public PlaceDetailsService(StoreRepository storeRepository,
                               StorePlaceMappingRepository mappingRepository,
                               PlacesClient placesClient) {
        this.storeRepository = storeRepository;
        this.mappingRepository = mappingRepository;
        this.placesClient = placesClient;
    }

    public PlaceDetailsDto getDetailsByStoreId(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "store not found"));

        Optional<StorePlaceMapping> mapped = mappingRepository.findFirstByStoreIdOrderByConfidenceDesc(storeId);
        String placeId = mapped.map(StorePlaceMapping::getPlaceId)
                .orElseGet(() -> autoMapPlaceId(store));

        if (placeId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "placeId mapping failed");
        }
        return getDetailsByPlaceId(placeId);
    }

    public PlaceDetailsDto getDetailsByPlaceId(String placeId) {
        PlaceDetailsDto dto = placesClient.getPlaceDetails(placeId);
        if (dto == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "place details not found");
        return dto;
    }

    private String autoMapPlaceId(Store store) {
        // 이름 + 행정구 + 도로명 주소를 input으로, 위치 바이어스는 좌표 사용
        String input = buildInputText(store);
        Point p = store.getGeom();
        double lng = (p != null) ? p.getX() : 127.1;
        double lat = (p != null) ? p.getY() : 37.3;

        String found = placesClient.findPlaceIdByText(input, lng, lat);
        if (found != null) {
            // 매칭 정확도 confidence = 0.8로 설정
            StorePlaceMapping entity = new StorePlaceMapping(store.getId(), found, 0.8, OffsetDateTime.now());
            mappingRepository.save(entity);
        }
        return found;
    }

    private String buildInputText(Store s) {
        StringBuilder sb = new StringBuilder();
        sb.append(s.getName() == null ? "" : s.getName()).append(" ");
        if (s.getSigungu() != null) sb.append(s.getSigungu()).append(" ");
        if (s.getRoadAddr() != null) sb.append(s.getRoadAddr());
        return sb.toString().trim();
    }
}
