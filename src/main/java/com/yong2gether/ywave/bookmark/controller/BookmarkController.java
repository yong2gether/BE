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
            @PathVariable Long storeId,
            @RequestBody(required = false) CreateReq req
    ) {
        Long id = bookmarkService.create(userId, storeId, (req != null ? req.groupId : null));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateRes(id, storeId, "가맹점 북마크가 생성되었습니다."));
    }

    public record CreateReq(Long groupId) {}

    public record CreateRes(Long bookmarkId, Long storeId, String message) {}
}
