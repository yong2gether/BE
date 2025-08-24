package com.yong2gether.ywave.preference.controller;

import com.yong2gether.ywave.preference.dto.MessageResponse;
import com.yong2gether.ywave.preference.service.PreferenceCategoryService;
import com.yong2gether.ywave.preference.dto.UpdatePreferredCategoriesRequest;
import com.yong2gether.ywave.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/preferences")
public class PreferenceController {

    private final PreferenceCategoryService service;
    private final UserRepository userRepository;


    @Operation(summary = "선호 카테고리 설정 API", description = "본인이 선호하는 카테고리를 설정하는 API입니다.")
    @PostMapping("/categories")
    public ResponseEntity<MessageResponse> setCategories(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestBody UpdatePreferredCategoriesRequest request
            ) {
        return ResponseEntity.ok(service.setCategories(userId, request.getCategoryIds()));
    }


    @Operation(summary = "본인이 선호한 카테고리 조회 API", description = "본인이 설정한 선호 카테고리들 전체 조회하는 API입니다.")
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories(@AuthenticationPrincipal(expression = "id") Long userId) {
        return ResponseEntity.ok(service.getCategories(userId));
    }


}
