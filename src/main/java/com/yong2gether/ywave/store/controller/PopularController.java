package com.yong2gether.ywave.store.controller;

import com.yong2gether.ywave.store.dto.PopularStoreDto;
import com.yong2gether.ywave.store.service.PopularStoreService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PopularController {

    private final PopularStoreService popularStoreService;

    public PopularController(PopularStoreService popularStoreService) { this.popularStoreService = popularStoreService; }



    // 예시 엔드포인트 api/v1/stores/popular?lng=127.1&lat=37.3&radius=2000&limit=30
    //              api/v1/stores/popular?lng=127.1&lat=37.3&radius=2000&limit=30&categories=FOOD,EDUCATION_STATIONERY&q=덮밥
    @Operation(summary = "가맹점 인기순 조회 API", description = "categories에 카테고리 넣으면 해당하는 가맹점들만 반환되고, q에는 키워드 넣으면 그에 속하는 가게명들 반환되는 API입니다. 검색으로도 쓸 수 있으니 참고 부탁해요.")
    @GetMapping("/stores/popular")
    public List<PopularStoreDto> popular(
            @RequestParam double lng,
            @RequestParam double lat,
            @RequestParam(defaultValue = "2000") int radius,
            @RequestParam(defaultValue = "30") int limit,
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) String q
    ) {
        List<String> cats = (categories == null || categories.isBlank())
                ? List.of() : Arrays.stream(categories.split(",")).map(String::trim).toList();

        return popularStoreService.popular(lng, lat, radius, limit, cats, q);
    }
}
