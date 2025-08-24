package com.yong2gether.ywave.bookmark.controller;

import com.yong2gether.ywave.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Swagger (springdoc-openapi) 문서화용
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "Bookmarks", description = "북마크 생성/취소 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores/{storeId}/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /** 북마크 생성 (groupId 없으면 기본 그룹) */
    @Operation(summary = "가맹점 북마크 생성", description = "헤더 X-USER-ID, Path storeId. body에 groupId 없으면 기본 그룹으로 저장")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공")
    })
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

    /** 북마크 취소 (문서화해서 Swagger에 Undocumented 안 뜨게) */
    @Operation(summary = "가맹점 북마크 취소", description = "헤더 X-USER-ID, Path storeId")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공")
    })
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
