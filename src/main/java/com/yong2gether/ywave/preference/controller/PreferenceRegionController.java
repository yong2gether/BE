package com.yong2gether.ywave.preference.controller;

import com.yong2gether.ywave.preference.dto.MessageResponse;
import com.yong2gether.ywave.preference.dto.RegionResponse;
import com.yong2gether.ywave.preference.dto.SetPreferredRegionRequest;
import com.yong2gether.ywave.preference.service.PreferenceRegionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/preferences")
public class PreferenceRegionController {

    private final PreferenceRegionService service;

    @Operation(summary = "선호 지역 설정 & 좌표 반환",
            description = "시/군/구/(선택)동을 입력하면 해당 지역 중심 좌표를 계산해 저장하고 반환합니다. 사용자는 1개만 저장됩니다.")
    @PostMapping("/regions")
    public ResponseEntity<RegionResponse> setRegion(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestBody SetPreferredRegionRequest request
    ) {
        return ResponseEntity.ok(service.setRegion(userId, request));
    }

    @Operation(summary = "현재 선호 지역 조회", description = "사용자에게 저장된 선호 지역 1건을 반환합니다.")
    @GetMapping("/regions")
    public ResponseEntity<RegionResponse> getRegion(
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        return service.getRegion(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
