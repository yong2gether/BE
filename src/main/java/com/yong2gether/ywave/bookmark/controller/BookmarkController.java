package com.yong2gether.ywave.bookmark.controller;

import com.yong2gether.ywave.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Swagger (springdoc-openapi)
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@Tag(name = "Bookmarks", description = "북마크 생성/취소 API")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/stores/{storeId}/bookmarks", produces = MediaType.APPLICATION_JSON_VALUE)
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * 북마크 생성 (groupId 없으면 기본 그룹)
     * - 201 Created + JSON 본문 반환
     */
    @Operation(
            summary = "가맹점 북마크 생성",
            description = "헤더 X-USER-ID, Path storeId. body에 groupId 없으면 기본 그룹으로 저장"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = CreateRes.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 북마크됨(서비스 정책에 따라)", content = @Content)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
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

    /**
     * 북마크 취소
     * - 200 OK + JSON 본문(삭제 여부) 반환
     */
    @Operation(
            summary = "가맹점 북마크 취소",
            description = "헤더 X-USER-ID, Path storeId"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 결과 반환",
                    content = @Content(schema = @Schema(implementation = DeleteRes.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    })
    @DeleteMapping
    public ResponseEntity<DeleteRes> delete(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long storeId
    ) {
        boolean deleted = bookmarkService.delete(userId, storeId);
        return ResponseEntity.ok(new DeleteRes(storeId, deleted));
    }

    // ====== DTOs ======
    public record CreateReq(Long groupId) {}

    public record CreateRes(
            @Schema(description = "생성된 북마크 ID") Long bookmarkId,
            @Schema(description = "가맹점 ID") Long storeId
    ) {}

    public record DeleteRes(
            @Schema(description = "가맹점 ID") Long storeId,
            @Schema(description = "삭제 여부 (이미 없었으면 false)") boolean deleted
    ) {}
}
