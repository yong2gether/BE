package com.yong2gether.ywave.recommend.controller;

import com.yong2gether.ywave.recommend.dto.RecommendedStore;
import com.yong2gether.ywave.recommend.service.RecommendationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService service;

    @GetMapping
    public ResponseEntity<List<RecommendedStore>> recommend(
            Principal principal,
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit
    ) {
        String email = principal.getName(); // 스프링 시큐리티 로그인 이메일
        return ResponseEntity.ok(service.recommendFor(email, limit));
    }
}
