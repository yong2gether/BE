package com.yong2gether.ywave.store.controller;

import com.yong2gether.ywave.store.repository.StoreRepository;
import com.yong2gether.ywave.store.service.CategoryClassifyService;
import com.yong2gether.ywave.store.service.PopularitySyncService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class CategoryAdminController {
    private final StoreRepository storeRepo;
    private final CategoryClassifyService classifySvc;
    private final PopularitySyncService popularitySvc;

    public CategoryAdminController(StoreRepository storeRepo,
                                   CategoryClassifyService classifySvc,
                                   PopularitySyncService popularitySvc) {
        this.storeRepo = storeRepo;
        this.classifySvc = classifySvc;
        this.popularitySvc = popularitySvc;
    }

    // 분류 안 된 상점 N개를 AI로 분류
    @PostMapping("/classify/all-once")
    public Map<String,Object> classifyAll(@RequestParam(defaultValue="200") int batchSize,
                                          @RequestParam(defaultValue="50") long sleepMs) {
        long before = storeRepo.countUncategorized();
        int done = classifySvc.classifyAllOnce(batchSize, sleepMs);
        long after = storeRepo.countUncategorized();
        return Map.of(
                "uncategorized_before", before,
                "processed", done,
                "uncategorized_after", after
        );
    }

    @PostMapping("/classify/by-signature")
    public Map<String,Object> classifyBySig(@RequestParam(defaultValue="2000") int sigLimit,
                                            @RequestParam(defaultValue="500")  int applyBatch){
        int estProcessed = classifySvc.classifyAllBySignature(sigLimit, applyBatch);
        return Map.of("estimated_applied", estProcessed);
    }

    // (선택) 구글 리뷰/평점 동기화
    @PostMapping("/popularity/sync")
    public Map<String,Object> syncPopularity(@RequestParam(defaultValue = "500") int limit) {
        int processed = popularitySvc.syncTopN(limit);
        return Map.of("processed", processed);
    }
}
