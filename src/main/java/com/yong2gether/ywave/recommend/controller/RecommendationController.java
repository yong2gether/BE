package com.yong2gether.ywave.recommend.controller;

import com.yong2gether.ywave.recommend.dto.RecommendedStore;
import com.yong2gether.ywave.recommend.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService service;

    @Operation(summary = "사용자가 입력한 선호 카테고리 기반 AI 추천 API", description = "사용자가 입력한 선호 카테고리 정보에 속하는 가맹점들을 추천해줍니다.")
    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendedStore>> recommend(
            Principal principal,
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit
    ) {
        String email = principal.getName(); // 스프링 시큐리티 로그인 이메일
        return ResponseEntity.ok(service.recommendFor(email, limit));
    }
}
