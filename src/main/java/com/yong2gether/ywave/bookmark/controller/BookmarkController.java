package com.yong2gether.ywave.bookmark.controller;

import com.yong2gether.ywave.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores/{storeId}/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<CreateRes> create(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long storeId,              // ★ storeId를 PathVariable로 받음
            @RequestBody(required = false) CreateReq req
    ) {
        Long bookmarkId = bookmarkService.create(
                userId,
                storeId,                              // ★ 서비스에 storeId 전달
                (req != null ? req.groupId : null)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateRes(bookmarkId, storeId));
    }

    public record CreateReq(Long groupId) {}
    public record CreateRes(Long bookmarkId, Long storeId) {}
}
