package com.yong2gether.ywave.store.service;

import com.yong2gether.ywave.bookmark.repository.BookmarkRepository;
import com.yong2gether.ywave.store.domain.Store;
import com.yong2gether.ywave.store.domain.StorePlaceMapping;
import com.yong2gether.ywave.store.dto.PlaceDetailsDto;
import com.yong2gether.ywave.store.repository.StorePlaceMappingRepository;
import com.yong2gether.ywave.store.repository.StoreRepository;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.ObjectProvider;
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

    private final ObjectProvider<BookmarkRepository> bookmarkRepositoryProvider;

    public PlaceDetailsService(StoreRepository storeRepository,
                               StorePlaceMappingRepository mappingRepository,
                               PlacesClient placesClient,
                               ObjectProvider<BookmarkRepository> bookmarkRepositoryProvider) {
        this.storeRepository = storeRepository;
        this.mappingRepository = mappingRepository;
        this.placesClient = placesClient;
        this.bookmarkRepositoryProvider = bookmarkRepositoryProvider;
    }


    // 로그인 사용자로 호출 시 userId 전달하면 북마크 플래그 세팅됨
    public PlaceDetailsDto getDetailsByStoreId(Long storeId, Long userId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "store not found"));

        // 1) placeId 매핑(예전 로직 그대로)
        Optional<StorePlaceMapping> mapped = mappingRepository.findFirstByStoreIdOrderByConfidenceDesc(storeId);
        String placeId = mapped.map(StorePlaceMapping::getPlaceId)
                .orElseGet(() -> autoMapPlaceId(store));

        if (placeId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "placeId mapping failed");
        }

        // 2) Google Details 그대로 가져오기
        PlaceDetailsDto dto = getDetailsByPlaceId(placeId);

        // 3) category/북마크 추가
        String category = storeRepository.findEffectiveCategoryByStoreId(storeId).orElse(null);
        dto.setCategory((category != null && !category.isBlank()) ? category : "기타");

        boolean bookmarked = false;
        if (userId != null) {
            BookmarkRepository br = bookmarkRepositoryProvider.getIfAvailable();
            if (br != null) {
                bookmarked = br.existsByUser_IdAndStore_Id(userId, storeId);
            }
        }
        dto.setBookmarked(bookmarked);

        return dto;
    }

    public PlaceDetailsDto getDetailsByPlaceId(String placeId) {
        PlaceDetailsDto dto = placesClient.getPlaceDetails(placeId);
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "place details not found");
        }
        return dto;
    }

    private String autoMapPlaceId(Store store) {
        String input = buildInputText(store);
        Point p = store.getGeom();
        double lng = (p != null) ? p.getX() : 127.1;
        double lat = (p != null) ? p.getY() : 37.3;

        String found = placesClient.findPlaceIdByText(input, lng, lat);
        if (found != null) {
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
