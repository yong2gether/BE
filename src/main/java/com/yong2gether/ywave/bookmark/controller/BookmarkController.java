package com.yong2gether.ywave.bookmark.controller;

import com.yong2gether.ywave.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores/{storeId}/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /** 북마크 생성 (groupId 없으면 기본 그룹) */
    @PostMapping
    public ResponseEntity<CreateRes> create(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long storeId,
            @RequestBody(required = false) CreateReq req
    ) {
        Long bookmarkId = bookmarkService.create(
                userId,
                storeId,
                (req != null ? req.groupId : null)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateRes(bookmarkId, storeId));
    }

    /** 북마크 취소 */
    @DeleteMapping
    public ResponseEntity<Void> delete(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long storeId
    ) {
        bookmarkService.delete(userId, storeId);
        return ResponseEntity.noContent().build();
    }

    public record CreateReq(Long groupId) {}
    public record CreateRes(Long bookmarkId, Long storeId) {}
}
