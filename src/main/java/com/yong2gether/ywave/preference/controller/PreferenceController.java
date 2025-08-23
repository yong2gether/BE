package com.yong2gether.ywave.preference.controller;

import com.yong2gether.ywave.preference.dto.MessageResponse;
import com.yong2gether.ywave.preference.dto.PreferenceService;
import com.yong2gether.ywave.preference.service.PreferenceCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/{userId}/preferences")
public class PreferenceController {

    private final PreferenceCategoryService service;


    @PostMapping("/categories")
    public ResponseEntity<MessageResponse> setCategories(
            @PathVariable Long userId,
            @RequestBody PreferenceService request
    ) {
        return ResponseEntity.ok(service.setCategories(userId, request.getCategories()));
    }


    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getCategories(userId));
    }
}
