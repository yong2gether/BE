package com.yong2gether.ywave.store.controller;

import com.yong2gether.ywave.auth.userdetails.CustomUserDetails;
import com.yong2gether.ywave.store.dto.NearbyStoreDto;
import com.yong2gether.ywave.store.dto.PlaceDetailsDto;
import com.yong2gether.ywave.store.service.PlaceDetailsService;
import com.yong2gether.ywave.store.service.StoreQueryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class StoreController {

    private final StoreQueryService storeQueryService;
    private final PlaceDetailsService placeDetailsService;

    public StoreController(StoreQueryService storeQueryService, PlaceDetailsService placeDetailsService) {
        this.storeQueryService = storeQueryService;
        this.placeDetailsService = placeDetailsService;
    }


    @Operation(summary = "사용자 위치 기반 주변 가맹점 조회 API", description = "사용자 위치를 좌표로 받고, 해당 범위 내의 가맹점들 리스트를 뽑아오는 APi입니다.")
    @GetMapping("/stores/nearby")
    public List<NearbyStoreDto> nearby(
            @RequestParam double lng,
            @RequestParam double lat,
            @RequestParam(defaultValue = "600") int radius,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String q
    ) {
        return storeQueryService.findNearby(lng, lat, radius, limit, q);
    }

    @Operation(summary = "특정 가맹점에 대한 상세 조회 API", description = "가맹점 storeId를 입력하면 Google Places API를 이용하여 상세 정보들을 전부 불러오는 API입니다. 로그인시 북마크 여부도 같이 반환합니다.")
    @GetMapping("/stores/{storeId}/details")
    public PlaceDetailsDto detailsByStore(@PathVariable Long storeId,
                                          @AuthenticationPrincipal(expression = "id") Long userId) {
        return placeDetailsService.getDetailsByStoreId(storeId, userId);
    }

    @Operation(summary = "placeId를 이용한 특정 가맹점에 대한 상세 조회 API", description = "구글에 등록된 placeId를 이용한 조회 API로 이것은 혹시 몰라 만든 API입니다.")
    @GetMapping("/places/{placeId}/details")
    public PlaceDetailsDto detailsByPlace(@PathVariable String placeId) {
        return placeDetailsService.getDetailsByPlaceId(placeId);
    }
}
