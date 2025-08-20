package com.yong2gether.ywave.store.controller;

import com.yong2gether.ywave.store.dto.PopularStoreDto;
import com.yong2gether.ywave.store.service.PopularStoreService;
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
